package observer.maven.plugins

import com.github.aymanizz.ktori18n.I18n
import io.ktor.server.application.Application
import io.ktor.server.application.install
import java.util.Locale

fun Application.configureLocalization() {
    install(I18n) {
        supportedLocales = listOf("en").map(Locale::forLanguageTag)
    }
}