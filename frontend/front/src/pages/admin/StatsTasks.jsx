import React, { useEffect, useState, useRef } from "react";
import Navbar from "../../components/layout/Navbar";
import { getTareasStats } from "../../api/endpoints/statsApi";

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

  // Collect current styles (linked stylesheets and style tags)
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
    // copy root classes (tailwind dark mode or custom themes)
    try {
      w.document.documentElement.className =
        document.documentElement.className || "";
      w.document.body.className = document.body.className || "";
    } catch {
      // ignore failures copying classes (can happen on some popup contexts)
      // fallback: proceed without copying root classes
    }

    const clone = element.cloneNode(true);
    // Append a header title similar to the app view
    const header = w.document.createElement("h1");
    header.textContent = title;
    header.style.fontFamily = "sans-serif";
    header.style.margin = "16px 0";
    w.document.body.insertBefore(header, w.document.body.firstChild);
    w.document.body.appendChild(clone);
  } catch {
    // fallback: write simple JSON
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

export default function StatsTasks() {
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
        const res = await getTareasStats();
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
    <div className="min-h-screen bg-linear-to-b from-yellow-50 to-white dark:from-gray-900 dark:to-gray-800">
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
          <h1 className="text-3xl font-extrabold">Estadísticas de Tareas</h1>
          <div className="flex gap-2">
            <button
              onClick={() => downloadJSON("tareas-stats.json", data)}
              className="px-3 py-2 bg-gray-900 text-white rounded"
            >
              JSON
            </button>
            <button
              onClick={() =>
                downloadCSV("tareas.csv", data?.user_performance || [])
              }
              className="px-3 py-2 bg-green-600 text-white rounded"
            >
              Excel (CSV)
            </button>
            <button
              onClick={() =>
                printPDF("Estadísticas de Tareas", printRef.current)
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
                <div className="text-sm">Tareas totales</div>
                <div className="text-3xl font-bold">{data.total_tasks}</div>
              </div>
              <div className="kpi bg-red-500 text-white rounded-lg p-4 shadow-lg">
                <div className="text-sm">Atrasadas</div>
                <div className="text-3xl font-bold">{data.overdue_tasks}</div>
              </div>
              <div className="kpi bg-green-500 text-white rounded-lg p-4 shadow-lg">
                <div className="text-sm">% completadas (global)</div>
                <div className="text-3xl font-bold">
                  {Math.round(
                    (Object.values(data.tasks_by_estado || {}).reduce(
                      (a, b) => a + b,
                      0
                    )
                      ? ((data.tasks_by_estado.COMPLETADA || 0) /
                          data.total_tasks) *
                        100
                      : 0) * 100
                  ) / 100}
                  %
                </div>
              </div>
            </div>

            <div className="mb-6 bg-white dark:bg-gray-800 p-4 rounded shadow">
              <h2 className="text-xl font-semibold mb-3">Tareas por estado</h2>
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
                        className="h-4 bg-yellow-500"
                      />
                    </div>
                    <div className="w-12 text-right">{v}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 p-4 rounded shadow">
              <h3 className="font-semibold mb-3">Rendimiento por usuario</h3>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left">
                    <th>Usuario</th>
                    <th>Asignadas</th>
                    <th>Completadas</th>
                    <th>Atrasadas</th>
                    <th>% complet.</th>
                  </tr>
                </thead>
                <tbody>
                  {(data.user_performance || []).map((u) => (
                    <tr key={u.usuario_id} className="border-t">
                      <td className="py-2">{u.nombre}</td>
                      <td>{u.assigned_count}</td>
                      <td>{u.completed_count}</td>
                      <td>{u.overdue_count}</td>
                      <td>{u.completion_rate}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
