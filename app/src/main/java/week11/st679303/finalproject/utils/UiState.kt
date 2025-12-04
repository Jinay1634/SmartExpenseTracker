package week7.st991662903.midpractice.utils

sealed class UiState {
    object Loading : UiState()
    object AuthRequired : UiState()
    object Authenticated : UiState()
    object ReportList : UiState()


}