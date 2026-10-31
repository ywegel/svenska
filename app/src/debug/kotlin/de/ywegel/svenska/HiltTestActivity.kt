package de.ywegel.svenska

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

// Host for Compose UI tests that need hiltViewModel(). Lives in debug, because Robolectric only resolves manifest activities.
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
