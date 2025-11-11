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

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {

    private List<Tarea> tareas = new ArrayList<>();
    private OnTareaActionListener listener;
    private boolean canUpdateStatus = false;

    public interface OnTareaActionListener {
        void onEstadoChanged(Tarea tarea, String nuevoEstado);
    }

    public TareasAdapter(OnTareaActionListener listener) {
        this.listener = listener;
    }

    public void setCanUpdateStatus(boolean canUpdate) {
        this.canUpdateStatus = canUpdate;
        notifyDataSetChanged();
    }

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

            // Configurar color del estado
            int colorEstado = getEstadoColor(tarea.getEstado());
            tvEstado.setBackgroundTintList(ColorStateList.valueOf(colorEstado));

            // Asignado
            if (tarea.getAsignado() != null) {
                tvAsignado.setText("Asignado a: " + tarea.getAsignado().getNombre());
            } else {
                tvAsignado.setText("Sin asignar");
            }

            // Fecha de vencimiento
            if (tarea.getFecha_vencimiento() != null && !tarea.getFecha_vencimiento().isEmpty()) {
                tvFechaVencimiento.setText(formatDate(tarea.getFecha_vencimiento()));
                // Verificar si está vencida
                if (isOverdue(tarea.getFecha_vencimiento())) {
                    tvFechaVencimiento.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                } else {
                    tvFechaVencimiento.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                }
            } else {
                tvFechaVencimiento.setText("");
            }

            // Mostrar botones de acción solo si puede actualizar estado y no está completada
            if (canUpdateStatus && !"COMPLETADA".equals(tarea.getEstado())) {
                layoutButtons.setVisibility(View.VISIBLE);
                setupButtons(tarea);
            } else {
                layoutButtons.setVisibility(View.GONE);
            }
        }

        private void setupButtons(Tarea tarea) {
            String estado = tarea.getEstado();

            btnProgreso.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEstadoChanged(tarea, "PROGRESO");
                }
            });

            btnCompletar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEstadoChanged(tarea, "COMPLETADA");
                }
            });

            // Configurar visibilidad según el estado actual
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
                case "PENDIENTE":
                    return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                case "PROGRESO":
                    return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                case "COMPLETADA":
                    return ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                default:
                    return ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            }
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }

        private boolean isOverdue(String dateString) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fechaVencimiento = format.parse(dateString);
                Date hoy = new Date();
                return fechaVencimiento != null && fechaVencimiento.before(hoy);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
