/* MAKE-GEN.C - Make generator matrix from parity-check matrix. */

/* Copyright (c) 1995-2012 by Radford M. Neal.
 *
 * Permission is granted for anyone to copy, use, modify, and distribute
 * these programs and accompanying documents for any purpose, provided
 * this copyright notice is retained and prominently displayed, and note
 * is made of any changes made to these programs.  These programs and
 * documents are distributed without any warranty, express or implied.
 * As the programs were written for research purposes only, they have not
 * been tested to the degree that would be advisable in any important
 * application.  All use of these programs is entirely at the user's own
 * risk.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#include "alloc.h"
#include "intio.h"
#include "open.h"
#include "mod2sparse.h"
#include "mod2dense.h"
#include "mod2convert.h"
#include "rcode.h"

static void usage(void);


#define LDPC_UNUSED_PARAMETER(parameter) do { (void)(parameter); } while (0)

/**
 * @brief MAIN PROGRAM. 
 */

int main(int argc, char **argv)
{
    LDPC_UNUSED_PARAMETER(argc);

    char *pchk_file, *gen_file, *other_gen_file;
    mod2sparse_strategy strategy = 0;
    int abandon_when, abandon_number;
    gen_make_method method = gen_unknown;
    char *meth;
    char junk;
    FILE *f;

    /* Look at arguments. */

    pchk_file = argv[1];
    gen_file = argv[2];
    meth = argv[3];

    if (!(pchk_file = argv[1])
        || !(gen_file = argv[2])
        || !(meth = argv[3]))
    {
        usage();
    }

    if (strcmp(meth, "sparse") == 0)
    {
        method = gen_sparse;
        strategy = Mod2sparse_minprod;
        abandon_number = 0;
        if (argv[4])
        {
            if (strcmp(argv[4], "first") == 0)        strategy = Mod2sparse_first;
            else if (strcmp(argv[4], "mincol") == 0)  strategy = Mod2sparse_mincol;
            else if (strcmp(argv[4], "minprod") == 0) strategy = Mod2sparse_minprod;
            else
            {
                usage();
            }
            if (argv[5])
            {
                if (sscanf(argv[5], "%d%c", &abandon_number, &junk) != 1 || abandon_number <= 0
                    || !argv[6]
                    || sscanf(argv[6], "%d%c", &abandon_when, &junk) != 1 || abandon_when <= 0
                    || argv[7])
                {
                    usage();
                }
            }
        }
    }
    else if (strcmp(meth, "dense") == 0)
    {
        method = gen_dense;
        other_gen_file = argv[4];
        if (other_gen_file && argv[5])
        {
            usage();
        }
    }
    else if (strcmp(meth, "mixed") == 0)
    {
        method = gen_mixed;
        other_gen_file = argv[4];
        if (other_gen_file && argv[5])
        {
            usage();
        }
    }
    else
    {
        usage();
    }

    /* Read parity check matrix. */

    mod2sparse * H = ldcp_pchk_read(pchk_file);
    int M = mod2sparse_rows(H);
    int N = mod2sparse_cols(H);

    if (N <= M)
    {
        fprintf(stderr, "Can't encode if number of bits (%d) isn't greater than number of checks (%d)\n", N, M);
        exit(1);
    }

    /* Create generator matrix file. */

    f = open_file_std(gen_file, "wb");
    if (f == NULL)
    {
        fprintf(stderr, "Can't create generator matrix file: %s\n", gen_file);
        exit(1);
    }


    /* Create generator matrix with specified method. */

    generator_matrix * gen_matrix = NULL;
    switch (method)
    {
    case gen_sparse:
    {
        gen_matrix = ldpc_generator_make_sparse(H, strategy, abandon_number, abandon_when);
        break;
    }
    case gen_dense:
    case gen_mixed:
    {
        gen_matrix = ldpc_generator_make_dense_mixed(H, method);
        break;
    }
    default:
        break;
    }

    if (!gen_matrix)
    {
        mod2sparse_free(H);
        fprintf(stderr, "Error generating to generator matrix file\n");
        exit(1);
    }
    /* Write it all to the generator matrix file. */
    ldpc_generator_write(gen_matrix, f);
    ldpc_generator_free(gen_matrix);


    /* Check for error writing file. */

    if (ferror(f) || fclose(f) != 0)
    {
        fprintf(stderr, "Error writing to generator matrix file\n");
        exit(1);
    }

    return 0;
}






/**
 * @briefPRINT USAGE MESSAGE AND EXIT. 
 */

static void usage(void)
{
    fprintf(stderr,
        "Usage:  make-gen pchk-file gen-file method\n");
    fprintf(stderr,
        "Method: sparse [ \"first\" | \"mincol\" | \"minprod\" ] [ abandon_num abandon_when ]\n");
    fprintf(stderr,
        "    or: dense [ other-gen-file ]\n");
    fprintf(stderr,
        "    or: mixed [ other-gen-file ]\n");
    exit(1);
}
