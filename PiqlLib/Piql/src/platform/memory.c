/*****************************************************************************
**
**  Definition of the memory interface
**
**  Creation date:  2016/06/15
**  Created by:     Ole Liabo
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the unboxing library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include <stdlib.h>
#include <stdio.h>
#include <Piql/inc/boxing/platform/platform.h>
#include "boxing/platform/memory.h"

// PUBLIC MEMORY FUNCTIONS
//

//----------------------------------------------------------------------------
/*!
 *  \param  size   Size of memory buffer to allocate in bytes.
 *
 *  Allocates a buffer of continious memory.
 */

void*   boxing_memory_allocate(size_t size)
{
    return malloc(size);
}


//----------------------------------------------------------------------------
/*! 
 *  \param  size   Size of memory buffer to allocate in bytes.
 *
 *  Allocates and clears (set all bytes to 0) a buffer of continious memory.
 */

void*   boxing_memory_allocate_and_clear( size_t size )
{
    void* buffer = malloc( size );
    memset( buffer, 0, size );
    return buffer;
}


//----------------------------------------------------------------------------
/*!
 *  \param  pointer_to_memory   A pointer to freed memory.
 *
 *  Frees the memory for a given pointer.
 */

void    boxing_memory_free(void* pointer_to_memory)
{
    if (pointer_to_memory != NULL)
    {
        free(pointer_to_memory);
    }
}


//----------------------------------------------------------------------------
/*!
 *  \param  pointer_to_memory   A pointer to cleared memory.
 *  \param  size_in_bytes       Size of cleared memory.
 *
 *  Clears (set all bytes to 0) continious memory for a given pointer.
 */

void    boxing_memory_clear(void* pointer_to_memory, size_t size_in_bytes)
{
    if (pointer_to_memory != NULL && size_in_bytes != 0)
    {
        memset(pointer_to_memory, 0, size_in_bytes);
    }
}


//----------------------------------------------------------------------------
/*!
 *  \param  pointer_to_memory_destination   A pointer to a destination memory area.
 *  \param  pointer_to_memory_source        A pointer to a source memory area.
 *  \param  size_in_bytes                   Size of copied memory.
 *
 *  Copied data from source memory area to destination memory area with a given size.
 */

void    boxing_memory_copy(void* pointer_to_memory_destination, const void* pointer_to_memory_source, size_t size_in_bytes)
{
    if (pointer_to_memory_destination != NULL && pointer_to_memory_source != NULL && size_in_bytes != 0)
    {
        memcpy(pointer_to_memory_destination, pointer_to_memory_source, size_in_bytes);
    }
}
