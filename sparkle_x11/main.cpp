#include <cstdio>
#include "were_thread.h"
#include "were_timer.h"
#include "sparkle.h"
#include "sparkle_x11.h"
#include "were_debug.h"
#include "were_signal_handler.h"
#include <csignal>
#include <ctime>



int seconds()
{
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec;
}

class test
{
public:

    ~test()
    {
    }

    test()
    {
        were_object_pointer<were_thread> thread(new were_thread());
        sparkle_ = were_object_pointer<sparkle>(new sparkle());
        sparkle_x11_ = were_object_pointer<sparkle_x11>(new sparkle_x11(sparkle_));

#if 1
        sig_ = were_object_pointer<were_signal_handler>(new were_signal_handler());

        were_object::connect(sig_, &were_signal_handler::signal, sig_, [this](uint32_t number)
        {
            if (number == SIGINT)
            {
                sig_.collapse();
                sparkle_x11_.collapse();
                sparkle_.collapse();
                were_thread::current_thread().collapse();
            }
        });
#endif
    }

    void run()
    {
        int prev_debug = 0;

        for (int i = 0; ; ++i)
        {
            were_thread::current_thread()->process(2000);

#if 0
            if (were_thread::current_thread().reference_count() == 2)
                break;
#endif

            int now = seconds();
            if (now > prev_debug)
            {
                printf("\033[2J"); // Clear screen
                printf("\033[0;0H"); // Move cursor
                were_debug_print_objects();
                prev_debug = now;
            }
        }
    }

private:
    //were_object_pointer<were_thread> thread_;
    were_object_pointer<sparkle> sparkle_;
    were_object_pointer<sparkle_x11> sparkle_x11_;
    were_object_pointer<were_signal_handler> sig_;
};

int main(int argc, char *argv[])
{
    test t;
    t.run();

    fprintf(stdout, "Done.\n");

    return 0;
}
