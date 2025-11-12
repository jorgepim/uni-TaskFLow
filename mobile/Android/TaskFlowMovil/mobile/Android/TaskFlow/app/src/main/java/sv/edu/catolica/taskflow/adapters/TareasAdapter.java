package sv.edu.catolica.taskflow.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import sv.edu.catolica.taskflow.R;
import sv.edu.catolica.taskflow.models.Proyecto;
import sv.edu.catolica.taskflow.models.Tarea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PROYECTO = 0;
    private static final int VIEW_TYPE_TAREA = 1;

    private List<Object> items = new ArrayList<>();
    private OnTareaActionListener listener;

    // --- CAMBIOS ---
    // private boolean canUpdateStatus = false; // Ya no usamos esto
    private int userId; // Guardamos el ID del usuario logueado

    public interface OnTareaActionListener {
        void onEstadoChanged(Tarea tarea, String nuevoEstado);
        void onEditClicked(Tarea tarea);
    }

    public TareasAdapter(OnTareaActionListener listener) {
        this.listener = listener;
    }

    // --- NUEVO MÉTODO ---
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // public void setCanUpdateStatus(boolean canUpdate) { ... } // Ya no es necesario

    /**
     * Lógica de expandir/contraer
     */
    public void setProyectos(List<Proyecto> proyectos) {
        items.clear();
        if (proyectos != null) {
            for (Proyecto p : proyectos) {
                if (p.getTareas() != null && !p.getTareas().isEmpty()) {
                    items.add(p);
                    if (p.isExpanded()) {
                        items.addAll(p.getTareas());
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Proyecto) {
            return VIEW_TYPE_PROYECTO;
        } else {
            return VIEW_TYPE_TAREA;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PROYECTO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyecto_header, parent, false);
            return new ProyectoViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
            return new TareaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_PROYECTO) {
            ((ProyectoViewHolder) holder).bind((Proyecto) items.get(position));
        } else {
            ((TareaViewHolder) holder).bind((Tarea) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder para el Proyecto (con lógica de expandir)
     */
    class ProyectoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProjectTitle;
        private ImageView ivArrow;

        public ProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            ivArrow = itemView.findViewById(R.id.ivArrow);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Proyecto proyecto = (Proyecto) items.get(position);
                boolean wasExpanded = proyecto.isExpanded();
                proyecto.setExpanded(!wasExpanded);

                ivArrow.animate().rotation(proyecto.isExpanded() ? 0 : -90).setDuration(200).start();

                if (proyecto.isExpanded()) {
                    List<Tarea> tareas = proyecto.getTareas();
                    if (tareas != null && !tareas.isEmpty()) {
                        items.addAll(position + 1, tareas);
                        notifyItemRangeInserted(position + 1, tareas.size());
                    }
                } else {
                    List<Tarea> tareas = proyecto.getTareas();
                    if (tareas != null && !tareas.isEmpty()) {
                        int count = tareas.size();
                        for (int i = 0; i < count; i++) {
                            items.remove(position + 1);
                        }
                        notifyItemRangeRemoved(position + 1, count);
                    }
                }
            });
        }

        void bind(Proyecto proyecto) {
            tvProjectTitle.setText(proyecto.getTitulo());
            ivArrow.setRotation(proyecto.isExpanded() ? 0 : -90);
        }
    }

    /**
     * ViewHolder para la Tarea (con lógica de botones MODIFICADA)
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

            // Clic para Editar (sin cambios)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClicked(tarea);
                }
            });

            // --- LÓGICA DE BOTONES MODIFICADA ---
            // Comprobar si el usuario logueado (this.userId) es el asignado a la tarea
            boolean isAsignado = (tarea.getAsignado() != null && tarea.getAsignado().getId() == TareasAdapter.this.userId);

            // Mostrar botones SÓLO si el usuario logueado es el asignado Y la tarea no está completada
            if (isAsignado && !"COMPLETADA".equals(tarea.getEstado())) {
                layoutButtons.setVisibility(View.VISIBLE);
                setupButtons(tarea);
            } else {
                // Ocultar para todos los demás (incluido el Creador)
                layoutButtons.setVisibility(View.GONE);
            }
        }

        // --- Métodos de ayuda (sin cambios) ---
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
            if (dateString == null) return "";
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
            if (dateString == null) return false;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fechaVencimiento = format.parse(dateString);
                Date hoy = new Date();
                if (tvEstado == null) return false;
                return fechaVencimiento != null && fechaVencimiento.before(hoy) && !tvEstado.getText().equals("COMPLETADA");
            } catch (Exception e) {
                return false;
            }
        }
    }
}