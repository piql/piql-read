/*****************************************************************************
**
**  Implementation of the referencebar interface
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
#include    "boxing/graphics/referencebar.h"

//  DEFINES
//

#define SMEMBER(member) (((boxing_reference_bar*)bar)->member)

//  PRIVATE INTERFACE
//

static void render(boxing_component * bar, boxing_painter * painter);

// PUBLIC REFERENCE BAR FUNCTIONS
//

void boxing_reference_bar_init(boxing_reference_bar * bar, enum boxing_align alignment, int reference_bar_freq_divider, int reference_bar_sync_distance, int reference_bar_sync_offset)
{
    boxing_component_init((boxing_component *)bar, NULL);
    bar->alignment = alignment;
    bar->reference_bar_freq_divider = reference_bar_freq_divider;
    bar->reference_bar_sync_distance = reference_bar_sync_distance;
    bar->reference_bar_sync_offset = reference_bar_sync_offset;
    bar->quiet_zone_size = 0;
    bar->base.render = render;
}

// PRIVATE REFERENCE BAR FUNCTIONS
//

static void render(boxing_component * bar, boxing_painter * painter) 
{
    int offset = SMEMBER(quiet_zone_size);
    int width = bar->size.x;
    int height = bar->size.y;
    int color[2] = {boxing_component_get_foreground_color(bar), boxing_component_get_background_color(bar)};
    if(SMEMBER(alignment) == BOXING_ALIGN_HORIZONTAL)
    {
        int sync_distance = SMEMBER(reference_bar_sync_distance) > 0 ? SMEMBER(reference_bar_sync_offset) : width;
        for(int i = 0; i < width; i++)
        {
            if(sync_distance > 0)
            {
                painter->draw_line(painter, i, offset, i, height-1-offset, color[(i/SMEMBER(reference_bar_freq_divider))%2]);
                if(i%SMEMBER(reference_bar_freq_divider) == (SMEMBER(reference_bar_freq_divider) - 1))
                    sync_distance--;
            }
            else
            {
                if(i%SMEMBER(reference_bar_freq_divider) == (SMEMBER(reference_bar_freq_divider) - 1))
                    sync_distance = SMEMBER(reference_bar_sync_distance) -1;
            }
        }
        for(int i = 0; i < SMEMBER(quiet_zone_size); i++)
        {
            painter->draw_line(painter, 0, offset+i, width-1, offset+i, color[0]);
            painter->draw_line(painter, 0, height-1-offset-i, width-1, height-1-offset-i, color[0]);
        }
    } 
    else if(SMEMBER(alignment) == BOXING_ALIGN_VERTICAL)
    {
        int sync_distance = SMEMBER(reference_bar_sync_distance) > 0 ? SMEMBER(reference_bar_sync_offset) : height;
        for(int i = 0; i < height; i++)
        {
            if(sync_distance > 0)
            {
                painter->draw_line(painter, offset, i, width-1-offset, i, color[(i/SMEMBER(reference_bar_freq_divider))%2]);
                if(i%SMEMBER(reference_bar_freq_divider) == (SMEMBER(reference_bar_freq_divider) - 1))
                    sync_distance--;
            }
            else
            {
                if(i%SMEMBER(reference_bar_freq_divider) == (SMEMBER(reference_bar_freq_divider) - 1))
                    sync_distance = SMEMBER(reference_bar_sync_distance) -1;
            }
        }
        for(int i = 0; i < SMEMBER(quiet_zone_size); i++)
        {
            painter->draw_line(painter, offset+i, 0, offset+i, height-1, color[0]);
            painter->draw_line(painter, width-1-offset-i, 0, width-1-offset-i, height-1, color[0]);
        }
    }

    boxing_component_render(bar, painter);
}
