/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

/**
 *
 * @author SAlbr
 */
public class Node {

    String name;

    public Node(String qName) {
        name = qName;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public void Addone() {
        num++;
    }
    int num = 1;
}
