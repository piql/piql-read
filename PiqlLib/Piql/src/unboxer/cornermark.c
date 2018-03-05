/*****************************************************************************
**
**  Implementation of the corner mark interface
**
**  Creation date:  2016/09/13
**  Created by:     Ole Liabo
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/unboxer/cornermark.h"

//----------------------------------------------------------------------------
/*!
 *  \ingroup unbox 
 *  \struct  frame_corner_marks_s  cornermark.h
 *  \brief   Corner points of the rectangular area.
 *  
 *  \param top_left      Top left corner point.
 *  \param top_right     Top right corner point.
 *  \param bottom_left   Bottom left corner point.
 *  \param bottom_right  Bottom right corner point.
 *
 *  The structure for storing the corner points of the rectangular area.
 */


//----------------------------------------------------------------------------
/*!
 *  \ingroup unbox
 *  \struct  boxing_corner_mark_definition_s  cornermark.h
 *  \brief   Corner marks definition.
 *
 *  \param symbol               Full size of 2x2 mosaic in pixels.
 *  \param quiet_zone           Number of black pixels around corner mark.
 *  \param vertical_distance    Distance between the center of the top_left and bottom_left corner marks (calculated by PIQL from frame height).
 *  \param horizontal_distance  Distance between the center of the top_left and top_right corner marks (calculated by PIQL from frame width).
 *  \param gap_between_frames   Distance between the center of bottom corner marks of nth frame and center of top corner marks of the n+1th frame.
 *
 *  Corner marks definition. All coordinates are given in printed coordinate space.
 */

//---------------------------------------------------------------------------- 
/*! \brief Calculate dimensions
 *
 *  Calculate dimensions of the rectangular area.
 *
 *  \ingroup unbox
 *  \param[in]  corner_marks  Corner points of the rectangular area.
 *  \return dimensions of the rectangular area.
 */

// PUBLIC CORNER MARK FUNCTIONS
//

boxing_pointi boxing_corner_mark_dimension( frame_corner_marks* corner_marks )
{
    boxing_pointi rectangle =
    {
        // assuming the sampling rate is the same at top and bottom 
        corner_marks->top_right.x - corner_marks->top_left.x,

        // assuming the sampling rate is the same at both sides 
        corner_marks->bottom_left.y - corner_marks->top_left.y
    };

    return rectangle;
}
