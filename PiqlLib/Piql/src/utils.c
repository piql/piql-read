/*****************************************************************************
**
**  Implementation of the utility interface
**
**  Creation date:  2016/06/28
**  Created by:     Piql
**
**
**  Copyright (c) 2016 Piql AS. All rights reserved.
**
**  This file is part of the boxingdata library
**
*****************************************************************************/

//  PROJECT INCLUDES
//
#include "boxing/utils.h"
#include "boxing/platform/memory.h"
#include "ghash.h"
#include "g_variant.h"

// PUBLIC UTILS FUNCTIONS
//

void boxing_utils_g_hash_table_destroy_item_string(void * data)
{
    boxing_memory_free(data);
}

void boxing_utils_g_hash_table_destroy_item_ghash(void * data)
{
    g_hash_table_destroy(data);
}

void boxing_utils_g_hash_table_destroy_item_g_variant(void * data)
{
    g_variant_free(data);
}
