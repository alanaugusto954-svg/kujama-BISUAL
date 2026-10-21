package com.kujama09.visuals;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/**
 * Contador de clics por segundo. Consulta el estado de los botones del ratón una vez por fotograma
 * (flanco de subida) y cuenta los clics de la última ventana de 1 segundo. No usa mixins.
 */
public final class CpsCounter {
    private static final long WINDOW_MS = 1000L;

    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftDown;
    private static boolean rightDown;

    private CpsCounter() {
    }

    /** Llamar una vez por fotograma. {@code listening} = false cuando hay una pantalla abierta. */
    public static void poll(long window, boolean listening) {
        long now = System.currentTimeMillis();
        boolean l = listening && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = listening && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !leftDown) {
            LEFT.addLast(now);
        }
        if (r && !rightDown) {
            RIGHT.addLast(now);
        }
        leftDown = l;
        rightDown = r;
        prune(LEFT, now);
        prune(RIGHT, now);
    }

    public static int left() {
        return LEFT.size();
    }

    public static int right() {
        return RIGHT.size();
    }

    private static void prune(ArrayDeque<Long> queue, long now) {
        while (!queue.isEmpty() && now - queue.peekFirst() > WINDOW_MS) {
            queue.pollFirst();
        }
    }
}
