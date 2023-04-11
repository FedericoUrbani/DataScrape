package org.example;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    /** record è un modo per creare una classe senza troppo impegno (aggiunto credo a java 13)**/
    record ChessPlayer(String name, String id, Integer rating, Integer quickRating){}

    public static void main(String[] args) throws IOException {

        /** come se stessi aprendo un browser **/

        WebClient client = new WebClient();
        /** si può disabilitare il js per evitare errori e anche il css**/
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);

        /** come se stessi andando in questo sito **/

        HtmlPage searchPage = client.getPage("https://new.uschess.org/player-search");

        /** trovo l'elemento a cui voglio puntare nell'html
         * (XPath ti consente di trovare elementi in una pagina html
         * in questo caso potrei usare getFormByName tuttavia non è il caso) **/

        HtmlForm form = (HtmlForm) searchPage.getByXPath("//form").get(0);

        /** <input data-drupal-selector="edit-display-name"
         * type="text" id="edit-display-name" name="display_name"
         * value="" size="60" maxlength="128" class="form-text">
         *
         * come si nota dalla parte di testo che gestisce l'inserimento può essere
         * preso il campo con il getinput by name inserendo display_name
         */
        HtmlInput displaynamefield = form.getInputByName("display_name");

        /** Prendo il nome del submit button per utilizzarlo **/
        HtmlInput submitButton = form.getInputByName("op");
        /** Scrivo il nome che voglio cercare per cliccare in seguito nel submit con questo nome **/
        displaynamefield.type("carlsen");
        /** clicco quindi il button**/
        /** ottengo la nuova pagina e la prendo utilizzando il metodo click**/
        HtmlPage resultPage= submitButton.click();
        /** creo un metodo parseResults per controllare la table derivante nella nuova pagina **/
        List<ChessPlayer>chessplayers= parseResults(resultPage);
        /** stampo il player**/
        for (ChessPlayer player: chessplayers){
            System.out.println(player);
        }
    }

    private static List<ChessPlayer> parseResults(HtmlPage resultPage) {
        /** in questo metodo (simile alla riga 30) trovo la table che voglio scrapare e ritorna una lista dove puntero al primo elemento**/
        HtmlTable table=(HtmlTable) resultPage.getByXPath("//table").get(0);
        /** il fatto che Bodies sia plurale implica che possiamo trovare più table rows
         *  ma puntiamo cmq alla prima perchè si vede che è la prima quindi mettiamo 0 nel get
         *  utilizziamo lo stream api per iterare i contenuti delle table row e implementare il contenuto
         *  nel ChessPlayer in relazione al posizionamento dei dati nelle row
         *  quindi siamo costretti ad utilizzare una mappa per passare da un data type ad un altro **/
        List <ChessPlayer>chessplayers = table.getBodies().get(0).getRows().stream().map(row ->{
            String rating = row.getCell(2).getTextContent();
            String quickrating = row.getCell(3).getTextContent();
            /** sono costretto a usare il return per inserire il player nella lista chessplayers)*/
            return new ChessPlayer(
                    /** ogni elemento della row è una cell quindi utilizziamo getCell con un indice (in questo caso è il name
                     * e utilizziamo getText per estrapolare il testo dalla cella
                     */
                    row.getCell(0).getTextContent(),
                    /** memeber id **/
                    row.getCell(1).getTextContent(),
                    /** regular rating c'è il rischio che sia null quindi creo una variabile che
                     * controlla a monte e con un ternario decido l'output a seguito del controllo
                     * sulla stringa fatto nel rigo 62 (dato che alcuni dati sono stringhe "" equivale a 1 e lo setto su 0)
                     */
                    rating.length()==0 ? null: Integer.parseInt(rating),
                    /** (riga 65)stesso ragionamento per il quick rating del giocatore */
                    quickrating.length()==0 ? null: Integer.parseInt(quickrating)
            );
        }).collect(Collectors.toList());
        return chessplayers;
    }

}
