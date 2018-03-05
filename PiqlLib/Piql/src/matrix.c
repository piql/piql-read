/*****************************************************************************
**
**  Implementation of the matrix interface
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
#include "boxing/matrix.h"
#include "boxing/log.h"
#include "boxing/platform/memory.h"

// PUBLIC MATRIX FUNCTIONS
//

boxing_matrix_float * boxing_matrix_float_multipage_create(unsigned int rows, unsigned int cols, unsigned int pages)
{
    if (cols == 0 || rows == 0)
    {
        return NULL;
    }

    boxing_matrix_float * matrix = BOXING_MEMORY_ALLOCATE_TYPE(boxing_matrix_float);
    matrix->cols = cols;
    matrix->rows = rows;
    matrix->pages = pages;
    matrix->data = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_float, cols * rows * pages);
    matrix->is_owning_data = DTRUE;
    if (matrix->data == NULL && cols * rows * pages != 0)
    {
        boxing_memory_free(matrix);
        DLOG_ERROR("failed to allocate image!");
        return NULL;
    }
    return matrix;
}

void boxing_matrix_float_free(boxing_matrix_float * matrix)
{
    if (matrix != NULL)
    {
        if (matrix->is_owning_data)
        {
            boxing_memory_free(matrix->data);
        }
        boxing_memory_free(matrix);
    }
}

boxing_matrixf * boxing_matrixf_create(unsigned int cols, unsigned int rows)
{
    if (cols == 0 || rows == 0)
    {
        return NULL;
    }

    boxing_matrixf * matrix = BOXING_MEMORY_ALLOCATE_TYPE(boxing_matrixf);
    matrix->width = cols;
    matrix->height = rows;
    matrix->data = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_pointf, cols * rows);
    matrix->is_owning_data = DTRUE;
    if (matrix->data == NULL && cols * rows != 0)
    {
        boxing_memory_free(matrix);
        DLOG_ERROR("failed to allocate image!");
        return NULL;
    }
    return matrix;
}

boxing_matrixi * boxing_matrixi_create(unsigned int cols, unsigned int rows)
{
    if (cols == 0 || rows == 0)
    {
        return NULL;
    }

    boxing_matrixi * matrix = BOXING_MEMORY_ALLOCATE_TYPE(boxing_matrixi);
    matrix->width = cols;
    matrix->height = rows;
    matrix->data = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY(boxing_pointi, cols * rows);
    matrix->is_owning_data = DTRUE;
    if (matrix->data == NULL && cols * rows != 0)
    {
        boxing_memory_free(matrix);
        DLOG_ERROR("failed to allocate image!");
        return NULL;
    }
    return matrix;
}

void boxing_matrixf_free(boxing_matrixf * matrix)
{
    if (matrix != NULL)
    {
        if (matrix->is_owning_data)
        {
            boxing_memory_free(matrix->data);
        }
        boxing_memory_free(matrix);
    }
}

void boxing_matrixi_free(boxing_matrixi * matrix)
{
    if (matrix != NULL)
    {
        if (matrix->is_owning_data)
        {
            boxing_memory_free(matrix->data);
        }
        boxing_memory_free(matrix);
    }
}

boxing_matrixf * boxing_matrixf_recreate(boxing_matrixf * matrix, unsigned int width, unsigned int height)
{
    if (width == 0 || height == 0)
    {
        return NULL;
    }

    if (matrix == NULL)
    {
        return boxing_matrixf_create(width, height);
    }
    else
    {
        if (matrix->width == width && matrix->height == height && matrix->data != NULL && matrix->is_owning_data)
        {
            return matrix;
        }
        else
        {
            if (matrix->is_owning_data && matrix->data != NULL)
            {
                boxing_memory_free(matrix->data);
            }
            boxing_matrixf_init_in_place(matrix, width, height);
            return matrix;
        }
    }
}

boxing_matrixi * boxing_matrixi_recreate(boxing_matrixi * matrix, unsigned int width, unsigned int height)
{
    if (width == 0 || height == 0)
    {
        return NULL;
    }

    if (matrix == NULL)
    {
        return boxing_matrixi_create(width, height);
    }
    else
    {
        if (matrix->width == width && matrix->height == height && matrix->data != NULL && matrix->is_owning_data)
        {
            return matrix;
        }
        else
        {
            if (matrix->is_owning_data && matrix->data != NULL)
            {
                boxing_memory_free(matrix->data);
            }
            boxing_matrixi_init_in_place(matrix, width, height);
            return matrix;
        }
    }
}

void boxing_matrixf_init_in_place(boxing_matrixf * matrix, unsigned int width, unsigned int height)
{
    if (matrix == NULL || width == 0 || height == 0)
    {
        return;
    }

    matrix->is_owning_data = DTRUE;
    matrix->width = width;
    matrix->height = height;
    matrix->data = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY( boxing_pointf, width * height);
    if (matrix->data == NULL && width * height != 0)
    {
        boxing_throw("failed to init matrixf in place!");
    }
}

void boxing_matrixi_init_in_place(boxing_matrixi * matrix, unsigned int width, unsigned int height)
{
    if (matrix == NULL || width == 0 || height == 0)
    {
        return;
    }

    matrix->is_owning_data = DTRUE;
    matrix->width = width;
    matrix->height = height;
    matrix->data = BOXING_MEMORY_ALLOCATE_TYPE_ARRAY( boxing_pointi, width * height );
    if (matrix->data == NULL && width * height != 0)
    {
        boxing_throw("failed to init matrixf in place!");
    }
}

boxing_matrixf * boxing_matrixf_copy(const boxing_matrixf * matrix)
{
    if (matrix == NULL)
    {
        return NULL;
    }

    boxing_matrixf * copy = boxing_matrixf_create(matrix->width, matrix->height);

    if (matrix->data == NULL)
    {
        boxing_memory_free(copy->data);
        copy->data = NULL;
    }
    else
    {
        boxing_memory_copy(copy->data, matrix->data, matrix->width * matrix->height * sizeof(boxing_pointf));
    }
    
    return copy;
}

boxing_matrixi * boxing_matrixi_copy(const boxing_matrixi * matrix)
{
    if (matrix == NULL)
    {
        return NULL;
    }

    boxing_matrixi * copy = boxing_matrixi_create(matrix->width, matrix->height);

    if (matrix->data == NULL)
    {
        boxing_memory_free(copy->data);
        copy->data = NULL;
    }
    else
    {
        boxing_memory_copy(copy->data, matrix->data, matrix->width * matrix->height * sizeof(boxing_pointi));
    }

    return copy;
}
