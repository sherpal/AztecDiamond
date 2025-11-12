package graphics

import graphics.DiamondDrawer.DominoBorderSizing
import graphics.DiamondDrawingOptions.DiamondOrderFontInfo

final case class DiamondMovieOptions(
    resolution: Int,
    diamondSizeTier: Int,
    drawDominoBorderUntil: Int,
    fullSizedDiamondStartingFromOrder: Int,
    dominoBorderSizing: DominoBorderSizing,
    drawDiamondEveryNOrder: Int,
    startDrawingAtOrder: Int,
    fontOrderInfo: DiamondOrderFontInfo
)
