/* MAKE-LDPC.C - Make a Low Density Parity Check code's parity check matrix. */

/* Copyright (c) 1995-2012 by Radford M. Neal and Peter Junteng Liu.
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
#include <string.h>
#include <math.h>

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

int main (int argc, char **argv)
{
    LDPC_UNUSED_PARAMETER(argc);

    pchk_make_method method = pchk_unknown;
    char *file, **meth;
    int seed, no4cycle;
    distrib *d = NULL;
    char junk;
    FILE *f;
    int M;
    int N;

    /* Look at initial arguments. */

    if (!(file = argv[1])
        || !argv[2] || sscanf(argv[2], "%d%c", &M, &junk) != 1 || M <= 0
        || !argv[3] || sscanf(argv[3], "%d%c", &N, &junk) != 1 || N <= 0
        || !argv[4] || sscanf(argv[4], "%d%c", &seed, &junk) != 1)
    {
        usage();
    }

    /* Look at the arguments specifying the method for producing the code. */

    meth = argv + 5;

    if (!meth[0]) usage();

    no4cycle = 0;

    if (strcmp(meth[0], "evencol") == 0 || strcmp(meth[0], "evenboth") == 0)
    {
        method = strcmp(meth[0], "evencol") == 0 ? pchk_evencol : pchk_evenboth;
        if (!meth[1])
        {
            usage();
        }
        d = distrib_create(meth[1]);
        if (d == 0)
        {
            usage();
        }
        if (meth[2])
        {
            if (strcmp(meth[2], "no4cycle") == 0)
            {
                no4cycle = 1;
                if (meth[3])
                {
                    usage();
                }
            }
            else
            {
                usage();
            }
        }
    }
    else
    {
        usage();
    }

    /* Check for some problems. */

    if (distrib_max(d) > M)
    {
        fprintf(stderr,
            "At least one checks per bit (%d) is greater than total checks (%d)\n",
            distrib_max(d), M);
        exit(1);
    }

    if (distrib_max(d) == M && N > 1 && no4cycle)
    {
        fprintf(stderr,
            "Can't eliminate cycles of length four with this many checks per bit\n");
        exit(1);
    }

    /* Make the parity check matrix. */

    mod2sparse * H = ldcp_pchk_make(seed, method, d, no4cycle, M, N);

    /* Write out the parity check matrix. */

    f = open_file_std(file, "wb");
    if (f == NULL)
    {
        fprintf(stderr, "Can't create parity check file: %s\n", file);
        exit(1);
    }

    intio_write(f, ('P' << 8) + 0x80);

    if (ferror(f) || !mod2sparse_write(f, H) || fclose(f) != 0)
    {
        fprintf(stderr, "Error writing to parity check file %s\n", file);
        exit(1);
    }

    return 0;
}


/**
 * @brief PRINT USAGE MESSAGE AND EXIT. 
 */

static void usage(void)
{
    fprintf(stderr, "Usage:  make-ldpc pchk-file n-checks n-bits seed method\n");
    fprintf(stderr, "Method: evencol  checks-per-col [ \"no4cycle\" ]\n");
    fprintf(stderr, "    or: evencol  checks-distribution [ \"no4cycle\" ]\n");
    fprintf(stderr, "    or: evenboth checks-per-col [ \"no4cycle\" ]\n");
    fprintf(stderr, "    or: evenboth checks-distribution [ \"no4cycle\" ]\n");
    exit(1);
}

