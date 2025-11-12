package sv.edu.catolica.taskflow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import sv.edu.catolica.taskflow.R;
import sv.edu.catolica.taskflow.models.Proyecto;

import java.util.ArrayList;
import java.util.List;

public class ProyectosStatsAdapter extends RecyclerView.Adapter<ProyectosStatsAdapter.ProyectoViewHolder> {

    private List<Proyecto> proyectos = new ArrayList<>();

    public void setProyectos(List<Proyecto> proyectos) {
        this.proyectos = proyectos != null ? proyectos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyecto_stats, parent, false);
        return new ProyectoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProyectoViewHolder holder, int position) {
        Proyecto proyecto = proyectos.get(position);
        holder.bind(proyecto);
    }

    @Override
    public int getItemCount() {
        return proyectos.size();
    }

    static class ProyectoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTituloProyecto, tvPorcentaje, tvTotalTareasProyecto, tvUsuariosAsignados;
        private ProgressBar progressBarProyecto;

        public ProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloProyecto = itemView.findViewById(R.id.tvTituloProyecto);
            tvPorcentaje = itemView.findViewById(R.id.tvPorcentaje);
            tvTotalTareasProyecto = itemView.findViewById(R.id.tvTotalTareasProyecto);
            tvUsuariosAsignados = itemView.findViewById(R.id.tvUsuariosAsignados);
            progressBarProyecto = itemView.findViewById(R.id.progressBarProyecto);
        }

        public void bind(Proyecto proyecto) {
            tvTituloProyecto.setText(proyecto.getTitulo());
            tvPorcentaje.setText(String.format("%.1f%%", proyecto.getPercent_completed()));
            tvTotalTareasProyecto.setText(proyecto.getTotal_tareas() + " tareas");
            tvUsuariosAsignados.setText(proyecto.getAssigned_users_count() + " usuarios");

            progressBarProyecto.setProgress((int) proyecto.getPercent_completed());
        }
    }
}
