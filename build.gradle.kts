plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// LaTeX thesis build tasks
// Build with local TeXLive (latexmk) or set LATEX_DOCKER=true to use Docker image

val thesisDir = file("thesis")

tasks.register<Exec>("thesisPdf") {
    group = "thesis"
    description = "Build thesis PDF (uses latexmk locally or Docker if LATEX_DOCKER=true)"
    workingDir = thesisDir
    val useDocker = System.getenv("LATEX_DOCKER")?.toBoolean() == true
    if (useDocker) {
        commandLine(
            "docker", "run", "--rm",
            "-v", "${thesisDir.absolutePath}:/workdir",
            "-w", "/workdir",
            "ghcr.io/xu-cheng/texlive-full",
            "latexmk", "-pdf", "-interaction=nonstopmode", "-synctex=1", "main.tex"
        )
    } else {
        commandLine("latexmk", "-pdf", "-interaction=nonstopmode", "-synctex=1", "main.tex")
    }
}

tasks.register<Exec>("thesisWatch") {
    group = "thesis"
    description = "Watch and rebuild thesis PDF on changes (latexmk -pvc)"
    workingDir = thesisDir
    val useDocker = System.getenv("LATEX_DOCKER")?.toBoolean() == true
    if (useDocker) {
        commandLine(
            "docker", "run", "--rm", "-it",
            "-v", "${thesisDir.absolutePath}:/workdir",
            "-w", "/workdir",
            "ghcr.io/xu-cheng/texlive-full",
            "latexmk", "-pdf", "-pvc", "-interaction=nonstopmode", "-synctex=1", "main.tex"
        )
    } else {
        commandLine("latexmk", "-pdf", "-pvc", "-interaction=nonstopmode", "-synctex=1", "main.tex")
    }
}

tasks.register<Exec>("thesisClean") {
    group = "thesis"
    description = "Clean LaTeX build artifacts"
    workingDir = thesisDir
    val useDocker = System.getenv("LATEX_DOCKER")?.toBoolean() == true
    if (useDocker) {
        commandLine(
            "docker", "run", "--rm",
            "-v", "${thesisDir.absolutePath}:/workdir",
            "-w", "/workdir",
            "ghcr.io/xu-cheng/texlive-full",
            "latexmk", "-C"
        )
    } else {
        commandLine("latexmk", "-C")
    }
}