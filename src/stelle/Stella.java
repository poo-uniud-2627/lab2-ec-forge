// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
// Consegna 5, variante 2: le stelle. I contratti ci sono, i campi e i corpi dei metodi li scrivete voi.
package stelle;

import geometria.Rettangolo;

/**
 * Una stella: compare nella corsia della racchetta del giocatore, resta visibile per un certo numero
 * di fotogrammi e poi sparisce. Se la racchetta la tocca mentre è visibile, la stella è raccolta.
 * La palla non la vede: ci passa attraverso.
 */
public class Stella {

    private final Rettangolo area;
    private final int durata;
    private int fotogrammi;
    private boolean raccolta;


    /**
     * Una stella nuova, visibile, nell'area data, che resta visibile per durata fotogrammi
     * (60 fotogrammi sono un secondo).
     * Lancia IllegalArgumentException se area è null o se durata è minore di 1.
     */
    public Stella(Rettangolo area, int durata) {
        if (area == null || durata < 1) {
            throw new IllegalArgumentException();
        }
        this.area = area;
        this.durata = durata;
        this.fotogrammi = 0;
        this.raccolta = false;
    }

    /** Dove sta la stella. Promette: non è null e non cambia mai. */
    public Rettangolo getArea() {
        return area;
    }

    /** Vero se si vede: non è stata raccolta e il suo tempo non è finito. */
    public boolean isVisibile() {
        return !raccolta && fotogrammi < durata;
    }

    /** Vero se è stata raccolta. */
    public boolean isRaccolta() {
        return raccolta;
    }

    /**
     * Passa un fotogramma. Promette: dopo durata chiamate la stella non è più visibile;
     * su una stella raccolta o sparita non cambia niente.
     */
    public void avanza() {
        if (raccolta) return;                 // su una raccolta non cambia niente
        if (fotogrammi < durata) fotogrammi++; // su una sparita non cambia niente
    }

    /**
     * La racchetta raccoglie la stella.
     * Promette: la stella è raccolta e non è più visibile.
     * Lancia IllegalStateException se la stella non è visibile (già raccolta, o sparita), e non cambia niente.
     */
    public void raccogli() {
        if (!isVisibile()) throw new IllegalStateException();
        raccolta = true;
    }
}
