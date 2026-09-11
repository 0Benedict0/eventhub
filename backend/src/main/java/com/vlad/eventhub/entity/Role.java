package com.vlad.eventhub.entity;

/** Глобальна роль користувача (на відміну від TaskFlow, тут роль не прив'язана до проєкту). */
public enum Role {
    ATTENDEE,   // бронює квитки
    ORGANIZER   // створює й керує подіями
}
