package de.ywegel.svenska.userflows

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltTestApplication
import de.ywegel.svenska.HiltTestActivity
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.navigation.AppNavigation
import de.ywegel.svenska.ui.theme.SvenskaTheme
import de.ywegel.svenska.userflows.robots.AddEditRobot
import de.ywegel.svenska.userflows.robots.ContainerRobot
import de.ywegel.svenska.userflows.robots.OverviewRobot
import de.ywegel.svenska.userflows.robots.SettingsRobot
import de.ywegel.svenska.userflows.robots.VocabularyDetailRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

// Subclasses still need @HiltAndroidTest, Hilt does not pick it up from the base class
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [Config.TARGET_SDK])
abstract class UserFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var containerRepository: ContainerRepository

    @Inject
    lateinit var vocabularyRepository: VocabularyRepository

    @Inject
    lateinit var database: VocabularyDatabase

    @Before
    fun injectDependencies() {
        hiltRule.inject()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    // Bypasses MainActivity, so onboarding and the consent sheet never show up
    protected fun launchApp(startRoute: Direction = ContainerScreenDestination) {
        composeRule.setContent {
            SvenskaTheme {
                AppNavigation(startRoute = startRoute)
            }
        }
    }

    protected fun givenContainer(name: String): VocabularyContainer = runBlocking {
        val id = containerRepository.upsertContainer(VocabularyContainer(name = name))
        VocabularyContainer(id = id.toInt(), name = name)
    }

    protected fun givenVocabulary(
        container: VocabularyContainer,
        word: String,
        translation: String,
        wordGroup: WordGroup,
    ): Unit = runBlocking {
        vocabularyRepository.upsertVocabulary(
            vocabulary(
                id = 0,
                containerId = container.id,
                word = word,
                translation = translation,
                wordGroup = wordGroup,
            ),
        )
    }

    protected fun container(block: ContainerRobot.() -> Unit) = ContainerRobot(composeRule).block()

    protected fun overview(block: OverviewRobot.() -> Unit) = OverviewRobot(composeRule).block()

    protected fun vocabularyDetail(block: VocabularyDetailRobot.() -> Unit) = VocabularyDetailRobot(composeRule).block()

    protected fun addEdit(block: AddEditRobot.() -> Unit) = AddEditRobot(composeRule).block()

    protected fun settings(block: SettingsRobot.() -> Unit) = SettingsRobot(composeRule).block()
}
