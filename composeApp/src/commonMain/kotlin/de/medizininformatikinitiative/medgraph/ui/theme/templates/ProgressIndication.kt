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
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl
import de.medizininformatikinitiative.medgraph.common.mvc.Progressable
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.tools.preview.TestOnlyProgressable

@Composable
@Preview
private fun ProgressIndication() {
    ApplicationTheme {
        ProgressIndication(TestOnlyProgressable(), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ProgressIndication(progressable: Progressable, modifier: Modifier = Modifier) {
    val model = remember { ProgressIndicationViewState(progressable) }
    if (progressable is NamedProgressable) {
        DetailedProgressIndication(model, modifier)
    } else {
        Column(modifier) {
            LinearProgressIndicator(model)
        }
    }
}

@Composable
private fun DetailedProgressIndication(state: ProgressIndicationViewState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.primaryTaskDescription)
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
        }
        LinearProgressIndicator(state)
        val minorStep = state.secondaryTaskDescription
        if (minorStep != null)
            Text(minorStep, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun LinearProgressIndicator(
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

private class ProgressIndicationViewState(
    progressable: Progressable
) : NamedProgressable.Listener {

    var progress by mutableStateOf(progressable.progress)
    var maxProgress by mutableStateOf(progressable.maxProgress)
    var primaryTaskDescription by mutableStateOf("")
    var secondaryTaskDescription by mutableStateOf<String?>(null)

    init {
        progressable.registerListener(this)
        if (progressable is NamedProgressable)
            onTaskStackChanged(progressable.currentTaskStack)
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