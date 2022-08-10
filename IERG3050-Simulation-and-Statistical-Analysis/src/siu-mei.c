#include "csim.h"
#include <stdio.h>

#define c	0.51473924	/* p.d.f. constant */
#define n	2.0	/* number of servers */

FACILITY f;
EVENT done;		/* pointer for counter */
TABLE tbl;		/*pointer for table */
QTABLE qtbl;	/*pointer for qhistogram */
FILE *fp;

double start_time, total_time;
double AUC_busy = 0, avg_busy = 0, proportion_busy;
int attempted_call = 0, blocked_call = 0;
double proportion_blocked;

void customers();
void init();
void printPerformance();
void reset_my_system();

void sim()
{
	int i;
	init();
	for (i = 0; i < 1; i++)	{
		create("sim"); /* make this a process */
		// reset_my_system();
		reset_prob(31000);
		start_time = simtime();
		while (simtime() < 7200.0)	/* 2 hours = 7200 seconds */
		{
			if (uniform(0.0, 1.6376137) < c)	{
				hold(exponential(1 / 0.048));
			} else	{
				hold(exponential(1 / 0.026));
			}
			customers();
		}
		
		total_time = simtime() - start_time;
		// avg_busy = AUC_busy / total_time;	/* time average = area under curve / total total */
		// proportion_busy = avg_busy / n;
		printPerformance();
	}
	// report();
}

void customers()
{
	TIME t1, elapsed;
	create("customer");
	t1 = clock;
	attempted_call++;
	note_entry(qtbl);
	
	use(f, uniform(0.0, 179.999999));
	elapsed=clock-t1;
	record(elapsed, tbl);
	AUC_busy = 1 * elapsed + AUC_busy;
	note_exit(qtbl);
	terminate();
}

void init()
{
	fp = fopen("MU2.txt", "a");
	set_output_file(fp);
	set_trace_file(fp);
	set_error_file(fp);
	set_model_name("M/M/1 Queue");

	f = facility_ms("server", n);	/* initialize facility */
	set_servicefunc(f, fcfs);
	// set_servicefunc(f, prc_shr);
	done = event("done");			/* initialize event */
	tbl = table("resp tms");		/* initialize table */
	qtbl = qtable("num in sys");	/* initialize qtable */
	// qtable_histogram(qtbl, 10, 0, 10);/* add histogram to qtable */
	// cnt = NARS;						/* initialize cnt */
}

void printPerformance()
{
	// fprintf(fp, "The time-average number of server that are busy:\t%.4f\n", avg_busy);
	// fprintf(fp, "The time-average proportion of server that are busy:\t%.4f\n", proportion_busy);
	// printf("The total number of attempted calls:\t%d\n", attempted_call);
	// printf("The number of calls that are blocked:\t%d\n", blocked_call);
	// printf("The proportion of calls that are blocked:\t%.4f\n", proportion_blocked);
	fprintf(fp, "%f, %d, %f, %d", resp(f), completions(f), qlen(f), qtable_max(qtbl));
	fprintf(fp, "\n");
}

void reset_my_system()
{
     create("reset");
     hold(10.0);/* wait for 10 units of time */
     reset();	/* clear all transient data */
}