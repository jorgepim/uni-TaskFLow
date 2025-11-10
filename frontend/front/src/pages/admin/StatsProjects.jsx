import React, { useEffect, useState, useRef } from "react";
import Navbar from "../../components/layout/Navbar";
import { getProyectosStats } from "../../api/endpoints/statsApi";

const downloadJSON = (filename, obj) => {
  const blob = new Blob([JSON.stringify(obj, null, 2)], {
    type: "application/json",
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
};

const downloadCSV = (filename, rows) => {
  // rows: array of objects
  if (!rows || rows.length === 0) {
    const blob = new Blob([""], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
    return;
  }
  const keys = Object.keys(rows[0]);
  const csv = [
    keys.join(","),
    ...rows.map((r) => keys.map((k) => JSON.stringify(r[k] ?? "")).join(",")),
  ].join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
};

const printPDF = (title, element) => {
  if (!element) return;
  const w = window.open("", "_blank");
  if (!w) return;

  const styles = Array.from(
    document.querySelectorAll('link[rel="stylesheet"], style')
  )
    .map((n) => n.outerHTML)
    .join("\n");

  const printFix = `<style>@media print{*{-webkit-print-color-adjust:exact;color-adjust:exact!important}}@media all{.no-print{display:none}}</style>`;

  w.document.write(`<html><head><title>${title}</title>`);
  w.document.write(
    '<meta name="viewport" content="width=device-width, initial-scale=1"/>'
  );
  w.document.write(styles + printFix);
  w.document.write(`</head><body></body></html>`);
  w.document.close();

  try {
    try {
      w.document.documentElement.className =
        document.documentElement.className || "";
      w.document.body.className = document.body.className || "";
    } catch {
      // ignore
    }

    const clone = element.cloneNode(true);
    const header = w.document.createElement("h1");
    header.textContent = title;
    header.style.fontFamily = "sans-serif";
    header.style.margin = "16px 0";
    w.document.body.insertBefore(header, w.document.body.firstChild);
    w.document.body.appendChild(clone);
  } catch {
    w.document.body.innerHTML = `<pre>${JSON.stringify(
      element,
      null,
      2
    )}</pre>`;
  }

  w.focus();
  setTimeout(() => {
    w.print();
  }, 500);
};

export default function StatsProjects() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);
  const printRef = useRef(null);

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();
  const userName = currentUser?.nombre || currentUser?.name || "Admin";

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getProyectosStats();
        setData(res.data?.data || null);
      } catch (err) {
        setError(
          err?.response?.data?.message ||
            err.message ||
            "Error cargando estadísticas"
        );
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  return (
    <div className="min-h-screen bg-linear-to-b from-indigo-50 to-white dark:from-gray-900 dark:to-gray-800">
      <Navbar
        brand="Admin Panel"
        userName={userName}
        links={[
          { to: "/admin/dashboard", label: "Inicio" },
          { to: "/admin/users", label: "Usuarios" },
          { to: "/admin/stats/projects", label: "Est. Proyectos" },
          { to: "/admin/stats/tasks", label: "Est. Tareas" },
        ]}
      />
      <main className="p-6 max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-extrabold">Estadísticas de Proyectos</h1>
          <div className="flex gap-2">
            <button
              onClick={() => downloadJSON("proyectos-stats.json", data)}
              className="px-3 py-2 bg-gray-900 text-white rounded"
            >
              JSON
            </button>
            <button
              onClick={() => downloadCSV("proyectos.csv", data?.projects || [])}
              className="px-3 py-2 bg-green-600 text-white rounded"
            >
              Excel (CSV)
            </button>
            <button
              onClick={() =>
                printPDF("Estadísticas de Proyectos", printRef.current)
              }
              className="px-3 py-2 bg-red-600 text-white rounded"
            >
              Exportar PDF
            </button>
          </div>
        </div>

        {loading && <div>Cargando estadísticas...</div>}
        {error && <div className="text-red-600">{String(error)}</div>}

        {data && (
          <section ref={printRef}>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <div className="kpi bg-indigo-600 text-white rounded-lg p-4 shadow-lg">
                <div className="text-sm">Proyectos totales</div>
                <div className="text-3xl font-bold">{data.total_projects}</div>
              </div>
              <div className="kpi bg-yellow-500 text-white rounded-lg p-4 shadow-lg">
                <div className="text-sm">Tareas totales</div>
                <div className="text-3xl font-bold">{data.total_tasks}</div>
              </div>
              <div className="kpi bg-green-500 text-white rounded-lg p-4 shadow-lg">
                <div className="text-sm">% completadas (global)</div>
                <div className="text-3xl font-bold">
                  {data.percent_completed_overall}%
                </div>
              </div>
            </div>

            <div className="mb-6 bg-white dark:bg-gray-800 p-4 rounded shadow">
              <h2 className="text-xl font-semibold mb-3">
                Tareas por estado (global)
              </h2>
              <div className="space-y-2">
                {Object.entries(data.tasks_by_estado || {}).map(([k, v]) => (
                  <div key={k} className="flex items-center gap-4">
                    <div className="w-32 text-sm font-medium">{k}</div>
                    <div className="flex-1 bg-gray-200 dark:bg-gray-700 rounded h-4 overflow-hidden">
                      <div
                        style={{
                          width: `${
                            (v / Math.max(1, data.total_tasks)) * 100
                          }%`,
                        }}
                        className="h-4 bg-indigo-500"
                      />
                    </div>
                    <div className="w-12 text-right">{v}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div className="bg-white dark:bg-gray-800 p-4 rounded shadow">
                <h3 className="font-semibold mb-3">
                  Top proyectos por completitud
                </h3>
                <ul className="space-y-3">
                  {(data.top_projects_by_completion || []).map((p) => (
                    <li
                      key={p.id}
                      className="p-3 border rounded flex items-center justify-between"
                    >
                      <div>
                        <div className="font-semibold">{p.titulo}</div>
                        <div className="text-xs text-gray-500">
                          Tareas: {p.total_tareas} — Usuarios:{" "}
                          {p.assigned_users_count}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-sm">{p.percent_completed}%</div>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="bg-white dark:bg-gray-800 p-4 rounded shadow">
                <h3 className="font-semibold mb-3">Proyectos</h3>
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left">
                      <th>Título</th>
                      <th>Tareas</th>
                      <th>% completadas</th>
                      <th>Usuarios</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(data.projects || []).map((p) => (
                      <tr key={p.id} className="border-t">
                        <td className="py-2">{p.titulo}</td>
                        <td>{p.total_tareas}</td>
                        <td>{p.percent_completed}%</td>
                        <td>{p.assigned_users_count}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
