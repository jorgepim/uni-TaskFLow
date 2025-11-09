import AppRoutes from './routes/AppRoutes'
import { ToastContainer } from 'react-toastify'

function App() {

  return (
    <>
      <ToastContainer
        position="bottom-right"
        autoClose={3000}
        hideProgressBar
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
      <AppRoutes />
    </>
  );
}

export default App
