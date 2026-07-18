package com.peopledb.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peopledb.app.data.TagWithCount
import com.peopledb.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onTagClick: (tagId: Long, isPlace: Boolean, tagName: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var tabIndex by remember { mutableIntStateOf(0) } // 0 = Places, 1 = Tags

    val places by (if (query.isBlank()) viewModel.observeTagsWithCount(true) else viewModel.searchTagsWithCount(query))
        .collectAsState(initial = emptyList())
    val genericTags by (if (query.isBlank()) viewModel.observeTagsWithCount(false) else viewModel.searchTagsWithCount(query))
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags & Places") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search tags & places") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            if (query.isBlank()) {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Places") },
                        icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Tags") },
                        icon = { Icon(Icons.Filled.Sell, contentDescription = null) }
                    )
                }
                val list = if (tabIndex == 0) places else genericTags
                TagList(list = list, onTagClick = onTagClick)
            } else {
                // Combined search results across both, places first.
                val combined = places + genericTags
                if (combined.isEmpty()) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text("No matching tags or places.")
                    }
                } else {
                    TagList(list = combined, onTagClick = onTagClick)
                }
            }
        }
    }
}

@Composable
private fun TagList(list: List<TagWithCount>, onTagClick: (Long, Boolean, String) -> Unit) {
    if (list.isEmpty()) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text("Nothing here yet.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LazyColumn {
        items(list, key = { it.id.toString() + it.isPlace }) { tag ->
            ListItem(
                leadingContent = {
                    Icon(
                        if (tag.isPlace) Icons.Filled.LocationOn else Icons.Filled.Sell,
                        contentDescription = null
                    )
                },
                headlineContent = { Text(tag.name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text("${tag.personCount} ${if (tag.personCount == 1) "person" else "people"}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableRow { onTagClick(tag.id, tag.isPlace, tag.name) }
            )
        }
    }
}

@Composable
private fun Modifier.clickableRow(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )
}
