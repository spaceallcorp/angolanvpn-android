// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.tailscale.ipn.R

// DotsMatrix represents the state of the progress indicator.
typealias DotsMatrix = List<List<Boolean>>

// The initial DotsMatrix that represents the Tailscale logo (T-shaped).
val logoDotsMatrix: DotsMatrix =
    listOf(
        listOf(false, false, false),
        listOf(true, true, true),
        listOf(false, true, false),
    )

@Composable
fun TailscaleLogoView(
    animated: Boolean = false,
    usesOnBackgroundColors: Boolean = false,
    modifier: Modifier
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Image(
        painter = painterResource(id = R.mipmap.logo_angolanvpn),
        contentDescription = "Angolan VPN Logo",
        modifier = Modifier.fillMaxSize()
    )
  }
}

val gameOfLife: List<DotsMatrix> =
    listOf(
        listOf(
            listOf(false, true, true),
            listOf(true, false, true),
            listOf(false, false, true),
        ),
        listOf(
            listOf(false, true, true),
            listOf(false, false, true),
            listOf(false, true, false),
        ),
        listOf(
            listOf(false, true, true),
            listOf(false, false, false),
            listOf(false, false, true),
        ),
        listOf(
            listOf(false, false, true),
            listOf(false, true, false),
            listOf(false, false, false),
        ),
        listOf(
            listOf(false, true, false),
            listOf(false, false, false),
            listOf(false, false, false),
        ),
        listOf(
            listOf(false, false, false),
            listOf(false, false, true),
            listOf(false, false, false),
        ),
        listOf(
            listOf(false, false, false),
            listOf(false, false, false),
            listOf(false, false, false),
        ),
        listOf(
            listOf(false, false, true),
            listOf(false, false, false),
            listOf(false, false, false),
        ),
        listOf(
            listOf(false, false, false),
            listOf(false, false, false),
            listOf(true, false, false),
        ),
        listOf(listOf(false, false, false), listOf(false, false, false), listOf(true, true, false)),
        listOf(listOf(false, false, false), listOf(true, false, false), listOf(true, true, false)),
        listOf(listOf(false, false, false), listOf(true, true, false), listOf(false, true, false)),
        listOf(listOf(false, false, false), listOf(true, true, false), listOf(false, true, true)),
        listOf(listOf(false, false, false), listOf(true, true, true), listOf(false, false, true)),
        listOf(listOf(false, true, false), listOf(true, true, true), listOf(true, false, true)))
