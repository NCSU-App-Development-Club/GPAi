import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adc.gpai.onboarding.OnboardingViewModel

class OnboardingViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}