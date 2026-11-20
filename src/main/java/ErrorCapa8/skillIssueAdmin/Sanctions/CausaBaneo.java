package ErrorCapa8.skillIssueAdmin.Sanctions;

import org.bukkit.Material;

public enum CausaBaneo {
    INSULTOS("Insultos", "Lenguaje ofensivo dirigido a otros jugadores", Material.ANVIL),
    TRAMPAS("Trampas", "Uso de clientes o modificaciones no permitidas", Material.IRON_SWORD),
    SPAM("Spam", "Mensajes repetidos o publicidad no autorizada", Material.PAPER),
    TOXICIDAD("Comportamiento tóxico", "Conducta hostil reiterada", Material.POISONOUS_POTATO),
    EXPLOITS("Uso de exploits", "Abuso de errores o vulnerabilidades del juego", Material.END_CRYSTAL),
    OTRA("Otra causa", "Especificar la causa en el chat", Material.WRITABLE_BOOK);

    private final String etiqueta;
    private final String descripcion;
    private final Material icono;
    CausaBaneo(String etiqueta, String descripcion, Material icono) {
        this.etiqueta = etiqueta; this.descripcion = descripcion; this.icono = icono;
    }
    public String etiqueta() { return etiqueta; }
    public String descripcion() { return descripcion; }
    public Material icono() { return icono; }
}
