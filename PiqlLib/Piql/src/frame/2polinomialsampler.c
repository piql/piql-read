/*****************************************************************************
**
**  Implementation of the 2polinomalsampler interface
**
**  Creation date:  2014/01/06
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
#include "2polinomialsampler.h"
#include "boxing/platform/memory.h"

//  PRIVATE INTERFACE
//

static boxing_image8 * sample(boxing_sampler * sampler, const boxing_image8 * image);
static boxing_image8 * sample5p(boxing_sampler * sampler, const boxing_image8 * image);

// PUBLIC 2 POLINOMIAL SAMPLER FUNCTIONS
//

boxing_sampler * boxing_2polinomialsampler_create(int width, int height)
{
    boxing_sampler * sampler = BOXING_MEMORY_ALLOCATE_TYPE(boxing_sampler);
    boxing_sampler_init(sampler, width, height);
    sampler->sample = sample;
    return sampler;
}


// PRIVATE 2 POLINOMIAL SAMPLER FUNCTIONS
//

static boxing_image8 * sample(boxing_sampler * sampler, const boxing_image8 * image)
{

    if ( boxing_image8_is_null(image) )
    {
        return NULL;
    }

    boxing_image8 * out_image = boxing_image8_create(sampler->location_matrix.width, sampler->location_matrix.height);
    boxing_image8_pixel * pixel = out_image->data;
    for(unsigned int i = 0; i < sampler->location_matrix.height; i++)
    {
        boxing_pointf *scan_line = MATRIX_ROW(&sampler->location_matrix, i);
        const boxing_pointf *scan_line_end = scan_line + sampler->location_matrix.width;
        for (; scan_line != scan_line_end; scan_line++)
        {
            boxing_float x = scan_line->x;
            boxing_float y = scan_line->y;

            // See the original code below, this is heavily refactored due to being in performance critical place.
            typedef boxing_float sampler_float;
            int xi = (int)x;
            int yi = (int)y;
            sampler_float m0, m1, m2, m3, m4, m5, m6, m7, m8;
            const boxing_image8_pixel * current_pixel = image->data + image->width * (yi - 1) + xi - 1;
            m0 = *(current_pixel++);
            m1 = *(current_pixel++);
            m2 = *current_pixel;

            current_pixel += image->width - 2;

            m3 = *(current_pixel++);
            m4 = *(current_pixel++);
            m5 = *current_pixel;

            current_pixel += image->width - 2;

            m6 = *(current_pixel++);
            m7 = *(current_pixel++);
            m8 = *current_pixel;

            sampler_float x_ = x - (int)x + 1;
            sampler_float y_ = y - (int)y + 1;
            sampler_float Z0, Z1, Z2;
            sampler_float B0, B1;
            sampler_float Y0 = y_*y_;
            sampler_float X0 = x_*x_;

            B0 = m0 * (sampler_float)0.5 - m3 + m6 * (sampler_float)0.5;
            B1 = m0 * (sampler_float)-1.5 + m3 * 2 + m6 * (sampler_float)-0.5;

            Z0 = Y0*B0 + y_*B1 + m0;

            B0 = m1 * (sampler_float)0.5 - m4 + m7 * (sampler_float)0.5;
            B1 = m1 * (sampler_float)-1.5 + m4 * 2 + m7 * (sampler_float)-0.5;

            Z1 = Y0*B0 + y_*B1 + m1;

            B0 = m2 * (sampler_float)0.5 - m5 + m8 * (sampler_float)0.5;
            B1 = m2 * (sampler_float)-1.5 + m5 * 2 + m8 * (sampler_float)-0.5;

            Z2 = Y0*B0 + y_*B1 + m2;

            B0 = Z0 * (sampler_float)0.5 - Z1 + Z2 * (sampler_float)0.5;
            B1 = Z0 * (sampler_float)-1.5 + Z1 * 2 + Z2 * (sampler_float)-0.5;




            /* float version
            sampler_float x_ = x - (int)x + 1;
            sampler_float y_ = y - (int)y + 1;
            sampler_float Z0, Z1, Z2;
            sampler_float B0, B1;
            sampler_float Y0 = y_*y_;
            sampler_float X0 = x_*x_;

            B0 = m0 * (sampler_float)0.5 - m3 + m6 * (sampler_float)0.5;
            B1 = m0 * (sampler_float)-1.5 + m3 * 2 + m6 * (sampler_float)-0.5;

            Z0 = Y0*B0 + y_*B1 + m0;

            B0 = m1 * (sampler_float)0.5 - m4 + m7 * (sampler_float)0.5;
            B1 = m1 * (sampler_float)-1.5 + m4 * 2 + m7 * (sampler_float)-0.5;

            Z1 = Y0*B0 + y_*B1 + m1;

            B0 = m2 * (sampler_float)0.5 - m5 + m8 * (sampler_float)0.5;
            B1 = m2 * (sampler_float)-1.5 + m5 * 2 + m8 * (sampler_float)-0.5;

            Z2 = Y0*B0 + y_*B1 + m2;

            B0 = Z0 * (sampler_float)0.5 - Z1 + Z2 * (sampler_float)0.5;
            B1 = Z0 * (sampler_float)-1.5 + Z1 * 2 + Z2 * (sampler_float)-0.5;


            int res = (int)(X0*B0 + x_*B1 + Z0);
            */
            /* // integer version
            int x_ = (x - (int)x + 1) * 255;
            int y_ = (y - (int)y + 1) * 255;
            int Z0, Z1, Z2;
            int B0, B1;
            int Y0 = (y_*y_) >> 8;//this increases errors
            int X0 = (x_*x_) >> 8;//this increases errors

            B0 = (m0 << 7) - (m3 << 8) + (m6 << 7);
            B1 = m0 * -384 + (m3 << 9) - (m6 << 7);

            Z0 = (Y0*B0 + y_*B1 + (m0 << 16)) >> 8;

            B0 = (m1 << 7) - (m4 << 8) + (m7 << 7);
            B1 = m1 * -384 + (m4 << 9) - (m7 << 7);

            Z1 = (Y0*B0 + y_*B1 + (m1 << 16)) >> 8;

            B0 = (m2 << 7) - (m5 << 8) + (m8 << 7);
            B1 = m2 * -384 + (m5 << 9) - (m8 << 7);

            Z2 = (Y0*B0 + y_*B1 + (m2 << 16)) >> 8;

            B0 = ((Z0 << 7) - (Z1 << 8) + (Z2 << 7)) >> 8;//this increases errors
            B1 = (Z0 * -384 + (Z1 << 9) - (Z2 << 7)) >> 8;//this increases errors

            int res = (X0*B0 + x_*B1 + (Z0 << 8)) >> 16;
            */

            /*
            //function z = sample(m, x, y)
            //  A = [ 0 0 1; 1 1 1; 4 2 1]
            //  Ai = inv(A)

            int xi = (int)x;
            int yi = (int)y;
            boxing_double m[3][3];

            m[0][0] = image.pixel(xi-1,   yi-1);
            m[0][1] = image.pixel(xi,     yi-1);
            m[0][2] = image.pixel(xi+1,   yi-1);

            m[1][0] = image.pixel(xi-1,   yi  );
            m[1][1] = image.pixel(xi,     yi  );
            m[1][2] = image.pixel(xi+1,   yi  );

            m[2][0] = image.pixel(xi-1,   yi+1);
            m[2][1] = image.pixel(xi,     yi+1);
            m[2][2] = image.pixel(xi+1,   yi+1);

            static boxing_double Ai[3][3] = {
                {  0.50000,  -1.00000,   0.50000 },
                { -1.50000,   2.00000,  -0.50000 },
                {  1.00000,   0.00000,   0.00000 },
            };
            boxing_double x_ = x - (int)x + 1;
            boxing_double y_ = y - (int)y + 1;
            boxing_double Z[3];
            boxing_double B[3];
            boxing_double Y[3] = {y_*y_, y_, 1};
            boxing_double X[3] = {x_*x_, x_, 1};

            B[0] = m[0][0] * Ai[0][0] + m[1][0] * Ai[0][1] + m[2][0] * Ai[0][2];
            B[1] = m[0][0] * Ai[1][0] + m[1][0] * Ai[1][1] + m[2][0] * Ai[1][2];
            B[2] = m[0][0] * Ai[2][0] + m[1][0] * Ai[2][1] + m[2][0] * Ai[2][2];

            Z[0] = Y[0]*B[0] + Y[1]*B[1] + Y[2]*B[2];

            B[0] = m[0][1] * Ai[0][0] + m[1][1] * Ai[0][1] + m[2][1] * Ai[0][2];
            B[1] = m[0][1] * Ai[1][0] + m[1][1] * Ai[1][1] + m[2][1] * Ai[1][2];
            B[2] = m[0][1] * Ai[2][0] + m[1][1] * Ai[2][1] + m[2][1] * Ai[2][2];

            Z[1] = Y[0]*B[0] + Y[1]*B[1] + Y[2]*B[2];

            B[0] = m[0][2] * Ai[0][0] + m[1][2] * Ai[0][1] + m[2][2] * Ai[0][2];
            B[1] = m[0][2] * Ai[1][0] + m[1][2] * Ai[1][1] + m[2][2] * Ai[1][2];
            B[2] = m[0][2] * Ai[2][0] + m[1][2] * Ai[2][1] + m[2][2] * Ai[2][2];

            Z[2] = Y[0]*B[0] + Y[1]*B[1] + Y[2]*B[2];

            B[0] = Z[0] * Ai[0][0] + Z[1] * Ai[0][1] + Z[2] * Ai[0][2];
            B[1] = Z[0] * Ai[1][0] + Z[1] * Ai[1][1] + Z[2] * Ai[1][2];
            B[2] = Z[0] * Ai[2][0] + Z[1] * Ai[2][1] + Z[2] * Ai[2][2];


            boxing_double res = X[0]*B[0] + X[1]*B[1] + X[2]*B[2];
            if(res < 0.0)
                res = 0.0;
            else if(res > 255.0)
                res = 255.0;
            return (int)res;*/
            
            int res = (int)(X0*B0 + x_*B1 + Z0);
            if (res > BOXING_PIXEL_MIN)
            {
                if (res < BOXING_PIXEL_MAX)
                {
                    *pixel++ = (boxing_image8_pixel)res;
                }
                else
                {
                    *pixel++ = BOXING_PIXEL_MAX;
                }
            }
            else
            {
                *pixel++ = BOXING_PIXEL_MIN;
            }
        }
    }

    return out_image;
}


const boxing_float sample5p_Mi[6][6] = {
    { -0.00833f,  0.04167f, -0.08333f,  0.08333f, -0.04167f,  0.00833f },
    {  0.12500f, -0.58333f,  1.08333f, -1.00000f,  0.45833f, -0.08333f },
    { -0.70833f,  2.95833f, -4.91667f,  4.08333f, -1.70833f,  0.29167f },
    {  1.87500f, -6.41667f,  8.91667f, -6.50000f,  2.54167f, -0.41667f },
    { -2.28333f,  5.00000f, -5.00000f,  3.33333f, -1.25000f,  0.20000f },
    {  1.00000f,  0.00000f,  0.00000f,  0.00000f,  0.00000f,  0.00000f },
};


static boxing_float sample5p_vector_dot_product(const boxing_float *a, const boxing_float *b, int length)
{
    boxing_float res = 0;
    while (length)
    {
        res += (*a) * (*b);
        length--;
        a++; 
        b++;
    }

    return res;
}

static void sample5p_matrix_vector_product(const boxing_float *matrix, const boxing_float *vecor, boxing_float *result, int rows, int cols)
{
    const boxing_float *matrix_row = matrix;
    for (int row = 0; row < rows; row++, matrix_row += cols)
    {        
        result[row] = sample5p_vector_dot_product(matrix_row, vecor, cols);
    }
}

static boxing_float sample5p_interpolate1d(const boxing_float *samples, boxing_float pos)
{
    boxing_float coeffs[6];
    sample5p_matrix_vector_product(&sample5p_Mi[0][0], samples, coeffs, 6, 6);

    boxing_float pow = pos;
    boxing_float res = coeffs[5];
    res += pow*coeffs[4];
    pow *= pos;
    res += pow*coeffs[3];
    pow *= pos;
    res += pow*coeffs[2];
    pow *= pos;
    res += pow*coeffs[1];
    pow *= pos;
    res += pow*coeffs[0];

    return res;
}


static boxing_image8 * sample5p(boxing_sampler * sampler, const boxing_image8 * image)
{

    if (boxing_image8_is_null(image))
    {
        return NULL;
    }

    boxing_float results[6];
    boxing_float samples[6];

    boxing_image8 * out_image = boxing_image8_create(sampler->location_matrix.width, sampler->location_matrix.height);
    boxing_image8_pixel * pixel = out_image->data;
    for (unsigned int i = 0; i < sampler->location_matrix.height; i++)
    {
        boxing_pointf *scan_line = MATRIX_ROW(&sampler->location_matrix, i);
        const boxing_pointf *scan_line_end = scan_line + sampler->location_matrix.width;
        for (; scan_line != scan_line_end; scan_line++)
        {
            boxing_float x = scan_line->x;
            boxing_float y = scan_line->y;

            // See the original code below, this is heavily refactored due to being in performance critical place.
            typedef boxing_float sampler_float;
            int xi = (int)x-2;
            int yi = (int)y-2;


            for (int hline = 0; hline < 6; hline++)
            {
                const boxing_image8_pixel * current_pixel = image->data + image->width * (yi + hline) + xi;
                for (int n = 0; n < 6; n++)
                {
                    samples[n] = (boxing_float)*current_pixel;
                    current_pixel++;
                }
                results[hline] = sample5p_interpolate1d(samples, x-xi);
            }

            int res = (int)(sample5p_interpolate1d(results, y-yi) + 0.5f); // +0.5 for rounding off to nearest int

            //int res = (int)(X0*B0 + x_*B1 + Z0);
            if (res > BOXING_PIXEL_MIN)
            {
                if (res < BOXING_PIXEL_MAX)
                {
                    *pixel++ = (boxing_image8_pixel)res;
                }
                else
                {
                    *pixel++ = BOXING_PIXEL_MAX;
                }
            }
            else
            {
                *pixel++ = BOXING_PIXEL_MIN;
            }
        }
    }

    return out_image;
}