/*****************************************************************************
**
**  Implementation of the calibrationbar interface
**
**  Creation date:  2014/12/16
**  Created by:     Haakon Larsson
**
**
**  Copyright (c) 2014 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/graphics/calibrationbar.h"

//  DEFINES
//

#define SMEMBER(member) (((boxing_calibration_bar*)bar)->member)

//  PRIVATE INTERFACE
//

static void calibration_bar_render(boxing_component * bar, boxing_painter * painter);
 
// PUBLIC CALIBRATION BAR FUNCTIONS
//

void boxing_calibration_bar_init(boxing_calibration_bar * bar, int levels_per_symbol)
{
    boxing_component_init((boxing_component*)bar, NULL);

    bar->base.render = calibration_bar_render;
    bar->levels_per_symbol = levels_per_symbol;
}

// PRIVATE CALIBRATION BAR FUNCTIONS
//

static void calibration_bar_render(boxing_component * bar, boxing_painter * painter)
{
    const int width = bar->size.x;
    const int height = bar->size.y;

    const boxing_float increment = (boxing_component_get_foreground_color(bar) - boxing_component_get_background_color(bar))/(boxing_float)(SMEMBER(levels_per_symbol) - 1); 
    boxing_float       color = (boxing_float)boxing_component_get_background_color(bar);

    boxing_float       patch_position = width/(boxing_float)SMEMBER(levels_per_symbol);
    int                last_x_pos = 0;

    for(int i = 0; i < SMEMBER(levels_per_symbol); i++)
    {
        const boxing_float patch_width = patch_position - last_x_pos;
        painter->fill_rect(painter, last_x_pos, 0, (int)patch_width, height, (int)color);

        color         += increment;
        last_x_pos      += (int)patch_width;
        patch_position += width / (boxing_float)SMEMBER(levels_per_symbol);
    }

    boxing_component_render(bar, painter);
}
