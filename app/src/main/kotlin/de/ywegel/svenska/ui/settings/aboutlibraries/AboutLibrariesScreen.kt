@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.settings.aboutlibraries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.navigation.SettingsNavGraph
import de.ywegel.svenska.ui.common.TopAppTextBar

@Destination<SettingsNavGraph>
@Composable
fun AboutLibrariesScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.settings_about_libraries_title),
                onNavigateUp = navigator::navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        )
    }
}
