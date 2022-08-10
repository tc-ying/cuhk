#include "csim.h"
#include <stdio.h>

#define n	5.0	/* number of servers */

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

void sim()
{
	int i;
	
	init();
	for (i = 0; i < 30; i++)	{
		create("sim"); /* make this a process */
		start_time = simtime();
		while (simtime() < 7200.0)	/* 2 hours = 7200 seconds */
		{
			hold(exponential(20.9));
			customers();
		}
		
		total_time = simtime() - start_time;
		avg_busy = AUC_busy / total_time;	/* time average = area under curve / total total */
		proportion_busy = avg_busy / n;
		proportion_blocked = (double)blocked_call / attempted_call;	
		printPerformance();

		rerun();
	}
	
	report();
}

void customers()
{
	TIME t1, elapsed;
	create("customer");
	t1 = clock;
	attempted_call++;
	note_entry(qtbl);
	
	use(f, uniform(0.0, 249.999999));
	// use(f, exponential(300));
	// use(f, erlang(222, 3300));
	elapsed=clock-t1;
	record(elapsed, tbl);
	AUC_busy = 1 * elapsed + AUC_busy;
	note_exit(qtbl);
	terminate();
}

void init()
{
	fp = fopen("food.out", "w");
	set_output_file(fp);
	set_trace_file(fp);
	set_error_file(fp);
	set_model_name("M/M/1 Queue");

	f = facility_ms("server", n);	/* initialize facility */
	set_servicefunc(f, fcfs);
	done = event("done");			/* initialize event */
	tbl = table("resp tms");		/* initialize table */
	qtbl = qtable("num in sys");	/* initialize qtable */
	// qtable_histogram(qtbl, 10, 0, 10);/* add histogram to qtable */
	// cnt = NARS;					/* initialize cnt */
}

void printPerformance()
{
	printf("The time-average number of lines that are busy:\t%.4f\n", avg_busy);
	printf("The time-average proportion of lines that are busy:\t%.4f\n", proportion_busy);
	printf("The total number of attempted calls:\t%d\n", attempted_call);
	printf("The number of calls that are blocked:\t%d\n", blocked_call);
	printf("The proportion of calls that are blocked:\t%.4f\n", proportion_blocked);
	printf("\n");
}