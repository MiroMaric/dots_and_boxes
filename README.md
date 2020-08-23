# Dots And Boxes
**Dots And Boxes** je popularna igra između dva igrača. Nju je prvi put opisao francuski matematičar **Édouard Lucas** 1889. godine. Složenost igre zavisi od dimenzije mreže za igru odnosno broja tačaka. Matematičari svrstavaju ovu igru u probleme NP težine.


| ![tabla](https://i.imgur.com/4pJVIzW.png =150x) |
|:-----------------------------------------------:|
| *Mreža za igru 4x4* |
## Opis igre
Igrači neizmenično povlače poteze. Igrač započinje igru tako što spoji bilo koje dve susedne tačke na tabli (spajanje tačaka dijagonalno nije dozvoljeno). Jedan potez igrača predstavlja stranicu kvadrata.
Kada igrač odigra potez koji zatvara kvadrat (4. ivica) taj kvadrat postaje njegov i označava se dogovorenim znakom. Pošto zatvori kvadrat, igrač nastavlja s potezima sve dok može ucrtati četvrtu stranicu, odnosno kompletirati kvadrat. Ako ne može, dužan je povući još jedan potez, pre nego što na red za igru dođe njegov protivnik.
Igra se završava kada su svi kvadrati zatvoreni a pobednik je onaj igrač koji je zatvorio više kvadrata.

### Primer igre
Kako bi se na najlakši način shvatila pravila, dat je primer odigrane partije između plavog i crvenog igrača u kojoj je plavi pobedio sa rezultatom **3:1**.

| ![](https://i.imgur.com/BmFm0BI.png =150x) | ![](https://i.imgur.com/zr726xw.png =150x) | ![](https://i.imgur.com/vcYdGvc.png =150x) | ![](https://i.imgur.com/VXLzXWy.png =150x) |
| :--:| :--:| :--:| :--:|
| *1. potez plavog*| *1. potez crvenog*| *2. potez plavog* | *2. potez crvenog* |

| ![](https://i.imgur.com/4B8WZ2o.png =150x) | ![](https://i.imgur.com/3LD0V17.png =150x) | ![](https://i.imgur.com/SovCrGx.png =150x) | ![](https://i.imgur.com/fZDtgLr.png =150x) | 
| :--:| :--:| :--:| :--:|
| *3. potez plavog* | *3. potez crvenog* | *4. potez plavog a)* | *4. potez plavog b)* |

| ![](https://i.imgur.com/ZFXYKYr.png =150x) | ![](https://i.imgur.com/7rsyf2H.png =150x) | ![](https://i.imgur.com/QhzQowU.png =150x) | ![](https://i.imgur.com/yBZGOh1.png =150x) | 
| :--:| :--:| :--:| :--:|
| *4. potez crvenog a)* | *4. potez crvenog b)* | *5. potez plavog a)* | *5. potez plavog b)* |

## DotsAndBoxes aplikacija
**DotsAndBoxes** je *stateless* web aplikacija koja izlaže skup endpoint-a. Osovna svrha aplikacije je da se na osnovu mreže (trenutna pozicija u igri) odigra "najbolji" naredni potez.
Pored narednog poteza, aplikacija izlaže niz dodatnih servisa koji olakšavaju implementaciju korisničkog interfejsa. Mreža za igru može biti bilo koje dimenzije ***dimension*** (dimension X dimension).
Aplikacija je implementirana u **Clojure** programskom jeziku.

### Opis logike
#### Struktura mreže
Struktura mreže je predstavljena dvodimenzionalnim vektorom. Vrednosti u prvom podvektoru predstavljaju horizontalne a u drugom vertikalne ivice kvadrata.

Na primer, sledeća pozicija u igri predstavljena je vektorom:

<table>
<tr><th> Pozicija </th><th>Vektorska reprezentacija</th></tr>
<tr><td>

![](https://i.imgur.com/VXLzXWy.png =150x)


</td><td>

**[ [false false false true false false] [true true false false true false]]**

</td></tr> </table>

Pozicija ivica susednih kvadrata zavise od dimenzije mreže kao i od toga da li je ivica horizontalna ili vertikalna.

<table>
<tr><th>Horizontalna ivica </th><th>Pozicije</th></tr>
<tr><td>

![](https://i.imgur.com/8Wmv5EF.png =150x) 


</td><td>

| Oznaka | Pozicija                                          |
| ------ | ------------------------------------------------- |
| I      | [0,position]                                      |
| DL     | [1,1+position+(position/(dimension-1))]           |
| D      | [0,position+dimension-1]                          |
| DD     | [1,position+(position/(dimension-1))]             |
| GD     | [1,position+dimension- (position/(dimension-1))]  |
| G      | [0,position-dimension+1]                          |
| GL     | [1,position+dimension-(position/(dimension-1))+1] |

</td></tr> </table>


<table>
<tr><th>Vertikalna ivica </th><th>Pozicije</th></tr>
<tr><td>

![](https://i.imgur.com/JV99VpI.png =230x)



</td><td>

| Oznaka | Pozicija                                      |
| ------ | --------------------------------------------- |
| I      | [1,position]                                  |
| DG     | [1,position-(position/dimension)]             |
| D      | [0,position+1]                                |
| DD     | [1,dimension+position-(position/dimension)-1] |
| LD     | [1,dimension+position-(position/dimension)-2] |
| L      | [0,position-1]                                |
| LG     | [1,position-(position/dimension-)-1]          |

</td></tr> </table>

#### Algoritam za odigravanje narednog poteza
Ulazni argument funkcije za odigravanje narednog poteza je vektorska reprezentacije mreže.
```clojure=
(defn next-move
  "odigrava naredni potez"
  [mesh]
    ...
  {:moves moves :squere new-mesh}
```
***U narednom delu ukratko će biti opisana logika njenog izvršava i pregled važnijih pomoćnih funkcija.***

Logika algoritma se sastoji iz narednih koraka:
* Provera validnosti vektorske reprezentacije mreže
* Rekurzivno zatvaranje svih kvadrata
<table>
<tr>
<th> Funkcija </th>
<th> Pozicija </th>
<th>Povratna vrednost</th>
<th>Mreža</th>
</tr>
<td>

close-boxes

</td>
<td>

![](https://i.imgur.com/7rsyf2H.png =100x)

</td>
<td>

{**:moves** [[0 2][1 3]] 
**:mesh** [
[true true true true true true]
[true true true true true true]
]}

</td>
<td>

![](https://i.imgur.com/yBZGOh1.png =100x)

</td>
</table>

* Nasumično odabiranje jednog slobodnog poteza (potez koji ne prouzrokuje zatvaranje kvadrta)

<table>
<tr>
<th> Funkcija </th>
<th> Pozicija </th>
<th>Povratna vrednost</th>
<th>Mreža</th>
</tr>
<td>

rand-free-move

</td>
<td>

![](https://i.imgur.com/zr726xw.png =100x)

</td>
<td>

[1 4]

</td>
<td>

![](https://i.imgur.com/vcYdGvc.png =100x)

</td>
</table>

* Ako ne postoji slobodan potez, odigravanje poteza koji nosi najmanji trošak

<table>
<tr>
<th> Funkcija </th>
<th> Pozicija </th>
<th>Povratna vrednost</th>
<th>Mreža</th>
</tr>
<td>

best-unplayed-move

</td>
<td>

![](https://i.imgur.com/4B8WZ2o.png =100x)

</td>
<td>

[0 1]

</td>
<td>

![](https://i.imgur.com/3LD0V17.png =100x)

</td>
</table>

### DotsAndBoxes API
| Endpoint       | Metod | Url                                               | Opis                                                                          |
| -------------- | ----- | ------------------------------------------------- | ----------------------------------------------------------------------------- |
| next-move      | POST  | **157.245.248.111/dots-and-boxes/next-move**      | Odigrava "najbolji" naredni potez                                             |
| close-move?    | POST  | **157.245.248.111/dots-and-boxes/is-close-move**  | Da li potez zatvara neku od susednih kvadrata i koja je pozicija tih kvadrata |
| all-free-moves | POST  | **157.245.248.111/dots-and-boxes/all-free-moves** | Svi slobodni potezi                                                           |
| get-move-boxes | POST  | **157.245.248.111/dots-and-boxes/get-move-boxes** | Vraća ivice kvadrata na koje dati potez ima efekat                            |

#### Primer API poziva


**best-unplayed-move**

```javascript=
//request
{
    "squere":[
        [true,false,false,false,false,false],
        [true,true,false,false,false,false]
    ]
} 
```  
```javascript=
//response
{
    "moves": [[0,2],[0,5]],
    "squere": [
        [true,false,true,false,false,true],
        [true,true,false,false,false,false]
    ]
} 
``` 
**close-move?**

```javascript=
//request
{
    "moves": [[0,2]],
    "squere": [
        [true,false,false,false,false,false],
        [true,true,false,false,false,false]
    ]
} 
```  
```javascript=
//response
//označava poziciju kvadrata koji se zatvara, u suprtonom -1
{
    1
} 
``` 

**all-free-moves**

```javascript=
//request
{
    "squere": [
        [true,false,false,false,false,false],
        [true,true,false,false,false,false]
    ]
}
``` 

```javascript=
//response
{
    [[0,1],[0,3],[0,4],[0,5],[1,2],[1,3],[1,4],[1,5]]
} 
``` 

**get-move-boxes**

```javascript=
//request
{
    "moves": [[0,2]],
    "squere": [
        [true,false,false,false,false,false],
        [true,true,false,false,false,false]
    ]
}
```  

```javascript=
//response
{
    [[false,false,false,false],[false,true,true,true]]
} 
``` 

## Linkovi
* [Demo aplikacije](https://drive.google.com/file/d/1_s97OXblP1r7DTy2TTwgjCdKZATJrkRN/view?usp=sharing)
* [Download igre](http://157.245.248.111/dotsandboxes/DotsAndBoxes.zip)

