/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package module.entities.NameFinder;

/**
 *
 * @author Sardianos
 */
public class EntityEntry {

    public String text;
    public String category;

    public EntityEntry(String text, String category) {
        this.text = text;
        this.category = category;
    }

    @Override
    public boolean equals(Object obj) {
        EntityEntry e = (EntityEntry) obj;
        return (this.text.equals(e.text) && this.category.equals(e.category));
    }

    @Override
    public int hashCode() {
        return (this.text+this.category).hashCode();
    }

}
