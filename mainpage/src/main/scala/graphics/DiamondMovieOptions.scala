package graphics

import graphics.DiamondDrawer.DominoBorderSizing

final case class DiamondMovieOptions(
    resolution: Int,
    diamondSizeTier: Int,
    drawDominoBorderUntil: Int,
    fullSizedDiamondStartingFromOrder: Int,
    dominoBorderSizing: DominoBorderSizing
)
