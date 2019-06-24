#ifndef SPARKLE_SEAT_H
#define SPARKLE_SEAT_H

#include "sparkle.h"
#include "generated/sparkle_wl_seat.h"
#include "sparkle_keyboard.h"
#include "were_thread.h" // XXX

class sparkle_seat : public sparkle_wl_seat
{
public:
    sparkle_seat(struct wl_client *client, int version, uint32_t id, were_object_pointer<sparkle_display> display) :
        sparkle_wl_seat(client, version, id), display_(display)
    {
        MAKE_THIS_WOP

        int caps = 0;
        caps |= WL_SEAT_CAPABILITY_KEYBOARD;
        //caps |= WL_SEAT_CAPABILITY_POINTER;
        //caps |= WL_SEAT_CAPABILITY_TOUCH;

        send_capabilities(caps);

        if (version >= WL_SEAT_NAME_SINCE_VERSION)
            send_name("Sparkle");

        were::connect(this_wop, &sparkle_seat::get_keyboard, this_wop, [this_wop](uint32_t id)
        {
            were_object_pointer<sparkle_keyboard> keyboard(new sparkle_keyboard(this_wop->client(), this_wop->version(), id, this_wop->display_));
            were::emit(this_wop, &sparkle_seat::keyboard_created, keyboard);
        });
    }

signals:
    were_signal<void (were_object_pointer<sparkle_keyboard> keyboard)> keyboard_created;

private:
    were_object_pointer<sparkle_display> display_;
};

#endif // SPARKLE_SEAT_H
