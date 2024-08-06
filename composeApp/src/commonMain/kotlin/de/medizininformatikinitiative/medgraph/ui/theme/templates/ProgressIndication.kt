package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressable
import de.medizininformatikinitiative.medgraph.common.mvc.Progressable
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.tools.preview.TestOnlyProgressable

@Composable
@Preview
private fun ProgressIndication() {
    val model = ProgressIndicationViewState()
    model.bind(TestOnlyProgressable())
    ApplicationTheme {
        DetailedProgressIndication(model, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun DetailedProgressIndication(
    state: ProgressIndicationViewState,
    modifier: Modifier = Modifier,
    indicateOngoingProgression: Boolean = true
) {
    Column(
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.primaryTaskDescription)
            if (indicateOngoingProgression) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            }
        }
        LinearProgressIndicaton(state)
        val minorStep = state.secondaryTaskDescription
        if (minorStep != null)
            Text(minorStep, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
fun LinearProgressIndicaton(
    state: ProgressIndicationViewState,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .height(12.dp)
) {
    LinearProgressIndicator(
        progress =
        state.progress * 1f / state.maxProgress,
        modifier = modifier
    )
}

class ProgressIndicationViewState : NamedProgressable.Listener {

    var progress by mutableStateOf(0)
    var maxProgress by mutableStateOf(1)
    var primaryTaskDescription by mutableStateOf("")
    var secondaryTaskDescription by mutableStateOf<String?>(null)

    /**
     * The [Progressable] currently bound to this instance or null if none is bound.
     */
    var progressable by mutableStateOf<Progressable?>(null)
        private set

    /**
     * Unbinds any previously bound [Progressable] and resets this view state to its default.
     */
    fun unbind() = bind(null)

    /**
     * Binds this view state to the given [Progressable], meaning its states always reflect the state of the given
     * [Progressable]. If this state was previously bound to a different instance, that binding becomes void.
     * If you pass null, you unbind this view state, causing it to reset to a default state.
     */
    fun bind(progressable: Progressable?) {
        val current = this.progressable
        if (current != null)
            current.unregisterListener(this)

        this.progressable = progressable

        if (progressable != null) {
            progressable.registerListener(this)
            onProgressChanged(progressable.progress, progressable.maxProgress)
            if (progressable is NamedProgressable)
                onTaskStackChanged(progressable.currentTaskStack)
        } else {
            resetToDefault()
        }
    }

    private fun resetToDefault() {
        progress = 0
        maxProgress = 1
        primaryTaskDescription = ""
        secondaryTaskDescription = null
    }

    override fun onProgressChanged(progress: Int, maxProgress: Int) {
        this.progress = progress
        this.maxProgress = maxProgress
    }

    override fun onTaskStackChanged(taskStack: Array<out String>) {
        this.primaryTaskDescription = taskStack.getOrElse(0) { "" }
        this.secondaryTaskDescription = taskStack.getOrElse(1) { null }
    }

}