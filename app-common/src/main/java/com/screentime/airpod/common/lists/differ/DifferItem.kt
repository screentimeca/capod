package com.screentime.airpod.common.lists.differ

import com.screentime.airpod.common.lists.ListItem

interface DifferItem : ListItem {
    val stableId: Long

    val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)?
        get() = null
}