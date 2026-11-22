// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import geometria.Rettangolo;
import giocatori.GiocatoreAutomatico;
import java.util.Random;
import ostacoli.Livello;
import ostacoli.MotoreDeiLivelli;
import ostacoli.Ostacoli;
import ostacoli.Ostacolo;
import stelle.Raccolta;
import stelle.Stella;

class Partita {
    static final int PUNTI_PER_VINCERE = 5;
    static final int SCARTO_MINIMO = 2;

    static final int VITE_INIZIALI = 3;
    static final double DIMENSIONE_STELLA = 16;
    static final int ATTESA_FRA_STELLE = 120;   // 2 secondi a 60 fps

    private final Campo campo;
    private final Giocatore giocatoreSinistra;
    private Giocatore giocatoreDestra;
    private final Racchetta racchettaSinistra;
    private final Racchetta racchettaDestra;
    private final Palla palla;
    private final Punteggio punteggio = new Punteggio(PUNTI_PER_VINCERE, SCARTO_MINIMO);
    private final Livelli livelli;
    private final Raccolta raccolta;
    private final Random rng = new Random();

    private Livello livello;
    private Ostacoli ostacoli;
    private boolean iniziata;

    private Stella stella;
    private int fotogrammiSenzaStella;

    Partita(Campo campo, Giocatore umano) {
        this.campo = campo;
        this.giocatoreSinistra = umano;
        double centroY = campo.getAltezza() / 2.0 - Racchetta.ALTEZZA / 2.0;
        this.racchettaSinistra = new Racchetta(30, centroY);
        this.racchettaDestra = new Racchetta(campo.getLarghezza() - 30 - Racchetta.LARGHEZZA, centroY);
        this.palla = new Palla(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        this.livelli = new Livelli(MotoreDeiLivelli.NUMERO_LIVELLI, VITE_INIZIALI, Lato.SINISTRA);
        this.raccolta = new Raccolta(Raccolta.STELLE_PER_UNA_VITA);
        preparaLivello(livelli.getLivello());
    }

    private void preparaLivello(int numero) {
        livello = MotoreDeiLivelli.livello(numero, campo.getLarghezza(), campo.getAltezza());
        ostacoli = livello.ostacoli();
        giocatoreDestra = new GiocatoreAutomatico(livello.erroreMassimo());
        punteggio.azzera();
        iniziata = false;
        palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        stella = null;
        fotogrammiSenzaStella = 0;
    }

    int getPunti(Lato chi) { return punteggio.getPunti(chi); }
    Lato getVincitore() { return punteggio.getVincitore(); }
    boolean isInCorso() { return iniziata && !punteggio.isFinita(); }
    boolean isIniziata() { return iniziata; }
    Livello getLivello() { return livello; }
    Ostacoli getOstacoli() { return ostacoli; }
    Livelli getLivelli() { return livelli; }
    Raccolta getRaccolta() { return raccolta; }
    Stella getStella() { return stella; }

    void avvia() {
        if (livelli.isFinito()) livelli.ricomincia();
        preparaLivello(livelli.getLivello());
        iniziata = true;
        lanciaPalla(new Random().nextBoolean());
    }

    private void lanciaPalla(boolean versoDestra) {
        palla.lancia(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0, versoDestra, livello.velocitaPalla());
    }

    void aggiorna() {
        if (!isInCorso()) return;
        ostacoli.avanzaTutti();
        muoviRacchette();
        palla.muovi();
        controllaRimbalzi();
        controllaOstacoli();
        controllaPunto();
        aggiornaStella();
    }

    private void muoviRacchette() {
        Mossa mossaSinistra = giocatoreSinistra.decidiMossa(palla, racchettaSinistra);
        Mossa mossaDestra = giocatoreDestra.decidiMossa(palla, racchettaDestra);
        racchettaSinistra.muovi(mossaSinistra, campo);
        racchettaDestra.muovi(mossaDestra, campo);
    }

    private void controllaRimbalzi() {
        double y = palla.getPosizione().y();
        boolean toccaAlto = y - palla.getRaggio() <= 0 && palla.getVelocitaY() < 0;
        boolean toccaBasso = y + palla.getRaggio() >= campo.getAltezza() && palla.getVelocitaY() > 0;
        if (toccaAlto || toccaBasso) palla.rimbalzaVerticale();

        if (palla.tocca(racchettaSinistra) && palla.getVelocitaX() < 0) palla.rimbalzaSu(racchettaSinistra);
        if (palla.tocca(racchettaDestra) && palla.getVelocitaX() > 0) palla.rimbalzaSu(racchettaDestra);
    }

    private void controllaOstacoli() {
        Ostacolo colpito = ostacoli.ostacoloCheTocca(palla.getArea());
        if (colpito != null) {
            palla.rimbalzaSu(colpito.getArea());
            colpito.colpisci(ostacoli);
        }
    }

    private void controllaPunto() {
        double x = palla.getPosizione().x();
        if (x < 0) {
            punteggio.segnaPunto(Lato.DESTRA);
            dopoIlPunto(true);
        } else if (x > campo.getLarghezza()) {
            punteggio.segnaPunto(Lato.SINISTRA);
            dopoIlPunto(false);
        }
    }

    private void dopoIlPunto(boolean versoSinistra) {
        if (punteggio.isFinita()) {
            palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
            livelli.partitaFinita(punteggio.getVincitore());
        } else {
            lanciaPalla(!versoSinistra);
        }
    }

    private void aggiornaStella() {
        // 1) c'è una stella visibile: falla avanzare, controlla se la racchetta la raccoglie
        if (stella != null && stella.isVisibile()) {
            Rettangolo areaR = new Rettangolo(racchettaSinistra.getX(), racchettaSinistra.getY(),
                    racchettaSinistra.getLarghezza(), racchettaSinistra.getAltezza());
            if (stella.getArea().tocca(areaR)) {
                stella.raccogli();
                if (raccolta.aggiungi()) livelli.guadagnaVita();
            } else {
                stella.avanza();
            }
        }
        // 2) non c'è (o è appena sparita): conta i fotogrammi e crea dopo un po'
        if (stella == null || !stella.isVisibile()) {
            if (stella != null) {
                stella = null;
                fotogrammiSenzaStella = 0;
            }
            fotogrammiSenzaStella++;
            if (fotogrammiSenzaStella >= ATTESA_FRA_STELLE) {
                creaStella();
            }
        }
    }

    private void creaStella() {
        double x = racchettaSinistra.getX()
                + racchettaSinistra.getLarghezza() / 2.0 - DIMENSIONE_STELLA / 2.0;
        double y = rng.nextDouble() * (campo.getAltezza() - DIMENSIONE_STELLA);
        stella = new Stella(new Rettangolo(x, y, DIMENSIONE_STELLA, DIMENSIONE_STELLA),
                livello.durataStella());
        fotogrammiSenzaStella = 0;
    }

    Campo getCampo() { return campo; }
    Palla getPalla() { return palla; }
    Racchetta getRacchettaSinistra() { return racchettaSinistra; }
    Racchetta getRacchettaDestra() { return racchettaDestra; }
}