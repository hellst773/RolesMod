package com.friends.rolesmod.role;

import java.util.List;

public enum Role {
    SANYA("sanya", "Саня Спермоглоткин", "Саня-анимешник",
            "Смотрит опенинги, коллекционирует фигурки, спорит про тайтлы и любит Японию.",
            new Task("house", "Построить дом в японском стиле (крыша, сакура)", true),
            new Task("skatepark", "Построить скейт-парк (рампы, перила)", true),
            new Task("trick", "Сделать трюк: прыгнуть с высоты 10+ блоков и приземлиться", false),
            new Task("katana", "Скрафтить алмазный меч и переименовать в «катана» (наковальня)", false)),

    ROLIK("rolik", "Ролик Хач", "Ролик-барыга",
            "Торгует всем, что плохо лежит, сидит в тг, лайкает мемы и кидает стикеры.",
            new Task("trade3", "Обменяться с 3 игроками (/role trade <игрок>)", false),
            new Task("sell_diamond", "Продать алмаз за 1 изумруд другому игроку (через /role trade)", false),
            new Task("village", "Найти деревню и поторговать с жителем", false),
            new Task("vapes", "Сделать ашкудики для Илюхи (скрафти ашкудику, можешь продавать всем)", false)),

    SASHA("sasha", "Саша Киска", "Саша-киска",
            "Говорит «может меня работа встр», ищет работу и вечно занят.",
            new Task("workplace", "Найти работу: построить рабочее место (стол, стул, табличка «РАБОТА»)", true),
            new Task("meet", "Встретиться с другим игроком (подойти на 5 блоков)", false),
            new Task("emeralds", "Заработать 10 изумрудов (держи в инвентаре)", false)),

    ILYUHA("ilyuha", "Илюха Альтуха", "Илюха-скейтер",
            "Любит кроссовки Osiris D3, вечно с одноразкой, ищет где пыхнуть.",
            new Task("osiris", "Найти/скрафтить «Osiris D3» (кожа + слизь)", false),
            new Task("vape", "Курить ашкудику каждые 2 минуты, 5 раз подряд (иначе ломка!)", false),
            new Task("volley", "Сделать фирменную подачу в волейболе", true));

    public record Task(String id, String text, boolean manual) {}

    public static final Task SUMMIT = new Task("summit",
            "ОБЩАЯ: собраться вчетвером на вершине горы и поставить табличку с никами (/role summit)", false);

    public final String id;
    public final String nick;
    public final String title;
    public final String character;
    private final List<Task> tasks;

    Role(String id, String nick, String title, String character, Task... tasks) {
        this.id = id;
        this.nick = nick;
        this.title = title;
        this.character = character;
        this.tasks = List.of(tasks);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Task findTask(String taskId) {
        for (Task t : tasks) {
            if (t.id().equals(taskId)) {
                return t;
            }
        }
        if (SUMMIT.id().equals(taskId)) {
            return SUMMIT;
        }
        return null;
    }

    public static Role byId(String id) {
        for (Role r : values()) {
            if (r.id.equals(id)) {
                return r;
            }
        }
        return null;
    }
}
