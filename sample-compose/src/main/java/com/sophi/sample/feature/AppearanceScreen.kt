package com.sophi.sample.feature

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.android.config.SophiLocale
import com.sophi.sample.AppearancePreset
import com.sophi.sample.EventLog
import com.sophi.sample.SampleSettings
import com.sophi.sample.SampleSophi

@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    FeatureScreen(
        title = "Görünüm",
        description = "Görünüm Sophi.configure'a verilen SophiConfig ile belirlenir; null bırakılan her alan tenant temasından gelir. " +
            "Bir preset seçince configure yeni config ile tekrar çağrılır ve açık sohbetler kapanır. Dil ise setLocale ile anında değişir.",
        onBack = onBack,
    ) {
        Text("Preset", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
        AppearancePreset.entries.forEach { preset ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = SampleSettings.preset == preset) {
                        SampleSettings.preset = preset
                        SampleSophi.configure(context)
                        EventLog.add("configure", "preset=${preset.name}")
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = SampleSettings.preset == preset, onClick = null)
                Text(preset.title, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Text("Dil", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
        SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp)) {
            SophiLocale.entries.forEachIndexed { index, locale ->
                SegmentedButton(
                    selected = SampleSettings.locale == locale,
                    onClick = {
                        SampleSettings.locale = locale
                        Sophi.setLocale(locale)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, SophiLocale.entries.size),
                ) { Text(locale.name) }
            }
        }

        Button(onClick = { Sophi.start(context) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Sohbeti aç")
        }
    }
}
