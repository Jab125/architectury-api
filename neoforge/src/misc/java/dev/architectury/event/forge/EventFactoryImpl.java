/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021, 2022 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package dev.architectury.event.forge;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class EventFactoryImpl {
    public static <T> Event<Consumer<T>> attachToForge(Event<Consumer<T>> event) {
        event.register(eventObj -> {
            if (!(eventObj instanceof net.neoforged.bus.api.Event)) {
                throw new ClassCastException(eventObj.getClass() + " is not an instance of forge Event!");
            }
            NeoForge.EVENT_BUS.post((net.neoforged.bus.api.Event) eventObj);
        });
        return event;
    }
    
    @ApiStatus.Internal
    public static <T> Event<EventActor<T>> attachToForgeEventActor(Event<EventActor<T>> event) {
        event.register(eventObj -> {
            if (!(eventObj instanceof net.neoforged.bus.api.Event)) {
                throw new ClassCastException(eventObj.getClass() + " is not an instance of forge Event!");
            }
            if (!(eventObj instanceof ICancellableEvent)) {
                throw new ClassCastException(eventObj.getClass() + " is not cancellable Event!");
            }
            NeoForge.EVENT_BUS.post((net.neoforged.bus.api.Event) eventObj);
            return EventResult.pass();
        });
        return event;
    }
    
    @ApiStatus.Internal
    public static <T> Event<EventActor<T>> attachToForgeEventActorCancellable(Event<EventActor<T>> event) {
        event.register(eventObj -> {
            if (!(eventObj instanceof net.neoforged.bus.api.Event)) {
                throw new ClassCastException(eventObj.getClass() + " is not an instance of forge Event!");
            }
            if (!(eventObj instanceof ICancellableEvent)) {
                throw new ClassCastException(eventObj.getClass() + " is not cancellable Event!");
            }
            if (((ICancellableEvent) NeoForge.EVENT_BUS.post((net.neoforged.bus.api.Event) eventObj)).isCanceled()) {
                return EventResult.interrupt(false);
            }
            return EventResult.pass();
        });
        return event;
    }
}
