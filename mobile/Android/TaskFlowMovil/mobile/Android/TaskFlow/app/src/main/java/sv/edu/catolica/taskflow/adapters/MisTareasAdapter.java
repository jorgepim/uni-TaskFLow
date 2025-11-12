package sv.edu.catolica.taskflow.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import sv.edu.catolica.taskflow.R;
import sv.edu.catolica.taskflow.models.Tarea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador simple para mostrar una LISTA PLANA de tareas
 * (Usado solo por MisTareasActivity)
 */
public class MisTareasAdapter extends RecyclerView.Adapter<MisTareasAdapter.TareaViewHolder> {

    private List<Tarea> tareas = new ArrayList<>();
    private OnTareaActionListener listener;
    private int userId; // ID del usuario logueado

    public interface OnTareaActionListener {
        void onEstadoChanged(Tarea tarea, String nuevoEstado);
        void onEditClicked(Tarea tarea);
    }

    public MisTareasAdapter(OnTareaActionListener listener) {
        this.listener = listener;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Método simple para setear la lista plana de tareas
    public void setTareas(List<Tarea> tareas) {
        this.tareas = tareas != null ? tareas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = tareas.get(position);
        holder.bind(tarea);
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    /**
     * ViewHolder para la Tarea
     */
    class TareaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitulo, tvDescripcion, tvEstado, tvAsignado, tvFechaVencimiento;
        private Button btnProgreso, btnCompletar;
        private View layoutButtons;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvAsignado = itemView.findViewById(R.id.tvAsignado);
            tvFechaVencimiento = itemView.findViewById(R.id.tvFechaVencimiento);
            btnProgreso = itemView.findViewById(R.id.btnProgreso);
            btnCompletar = itemView.findViewById(R.id.btnCompletar);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
        }

        public void bind(Tarea tarea) {
            tvTitulo.setText(tarea.getTitulo());
            tvDescripcion.setText(tarea.getDescripcion());
            tvEstado.setText(tarea.getEstado());
            int colorEstado = getEstadoColor(tarea.getEstado());
            tvEstado.setBackgroundTintList(ColorStateList.valueOf(colorEstado));

            if (tarea.getAsignado() != null) {
                tvAsignado.setText("Asignado a: " + tarea.getAsignado().getNombre());
            } else {
                tvAsignado.setText("Sin asignar");
            }

            if (tarea.getFecha_vencimiento() != null && !tarea.getFecha_vencimiento().isEmpty()) {
                tvFechaVencimiento.setText(formatDate(tarea.getFecha_vencimiento()));
                if (isOverdue(tarea.getFecha_vencimiento())) {
                    tvFechaVencimiento.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                } else {
                    tvFechaVencimiento.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                }
            } else {
                tvFechaVencimiento.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClicked(tarea);
                }
            });

            // Lógica de botones: Mostrar solo si YO soy el asignado
            boolean isAsignado = (tarea.getAsignado() != null && tarea.getAsignado().getId() == MisTareasAdapter.this.userId);

            if (isAsignado && !"COMPLETADA".equals(tarea.getEstado())) {
                layoutButtons.setVisibility(View.VISIBLE);
                setupButtons(tarea);
            } else {
                layoutButtons.setVisibility(View.GONE);
            }
        }

        private void setupButtons(Tarea tarea) {
            String estado = tarea.getEstado();
            btnProgreso.setOnClickListener(v -> listener.onEstadoChanged(tarea, "PROGRESO"));
            btnCompletar.setOnClickListener(v -> listener.onEstadoChanged(tarea, "COMPLETADA"));
            if ("PENDIENTE".equals(estado)) {
                btnProgreso.setVisibility(View.VISIBLE);
                btnCompletar.setVisibility(View.VISIBLE);
            } else if ("PROGRESO".equals(estado)) {
                btnProgreso.setVisibility(View.GONE);
                btnCompletar.setVisibility(View.VISIBLE);
            } else {
                btnProgreso.setVisibility(View.GONE);
                btnCompletar.setVisibility(View.GONE);
            }
        }
        private int getEstadoColor(String estado) {
            switch (estado) {
                case "PENDIENTE": return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                case "PROGRESO": return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                case "COMPLETADA": return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                default: return ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            }
        }
        private String formatDate(String dateString) {
            if (dateString == null) return "";
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return out.format(in.parse(dateString));
            } catch (Exception e) { return dateString; }
        }
        private boolean isOverdue(String dateString) {
            if (dateString == null) return false;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fechaVencimiento = format.parse(dateString);
                Date hoy = new Date();
                if (tvEstado == null) return false;
                return fechaVencimiento != null && fechaVencimiento.before(hoy) && !tvEstado.getText().equals("COMPLETADA");
            } catch (Exception e) { return false; }
        }
    }
}