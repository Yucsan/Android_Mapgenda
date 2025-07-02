import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel

class UbicacionViewModelFactory(
    private val application: Application,
    private val repository: UbicacionRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UbicacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UbicacionViewModel(application, repository, usuarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
