package de.medizininformatikinitiative.medgraph.ui.tools.preview

import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl

/**
 * @author Markus Budeus
 */
class TestOnlyProgressable : NamedProgressableImpl() {

    init {
        setTaskStack("Doing something", "Doing a subtask of something...")
        progress = 4
        maxProgress = 7
    }

    override public fun setTaskStack(vararg taskStack: String) {
        super.setTaskStack(*taskStack)
    }

    override public fun setProgress(progress: Int) {
        super.setProgress(progress)
    }

    override public fun setMaxProgress(maxProgress: Int) {
        super.setMaxProgress(maxProgress)
    }

}