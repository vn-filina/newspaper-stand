package ru.kiosk.ui;

import ru.kiosk.api.InventoryApi;
import ru.kiosk.dto.*;
import java.time.LocalDate;
import java.util.Scanner;
public class ConsoleController {
    private final InventoryApi api;
    private final Scanner in = new Scanner(System.in);

    public ConsoleController(InventoryApi api) { this.api = api; }

    public void run() {
        System.out.println("=== Газетный киоск ===");
        while (true) {
            System.out.println("""
                1) Приёмка товара
                2) Продажа
                3) Редактировать товар
                4) Показать остатки
                0) Выход
                """);
            System.out.print("Выбор: ");
            String cmd = in.nextLine().trim();
            try {
                switch (cmd) {
                    case "1" -> receipt();
                    case "2" -> sale();
                    case "3" -> update();
                    case "4" -> api.findAll().forEach(System.out::println);
                    case "0" -> { System.out.println("Сохраняем и выходим..."); return; }
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
    private void receipt() {
        ProductDto dto = readProductDto();
        System.out.print("Количество (шт): ");
        int qty = Integer.parseInt(in.nextLine().trim());
        var res = api.receipt(new ReceiptRequest(dto, qty));
        System.out.println(res.message());
    }

    private void sale() {
        ProductDto dto = readProductDto();
        System.out.print("Сколько продать (шт): ");
        int qty = Integer.parseInt(in.nextLine().trim());
        var res = api.sale(new SaleRequest(dto, qty));
        System.out.println(res.message());
    }

    private void update() {
        System.out.println("-- Ключ товара (текущие значения) --");
        ProductDto key = readProductDto();
        System.out.println("-- Новые значения (пусто = не менять) --");
        ProductDto patch = readPartialProduct();
        var res = api.update(new UpdateRequest(key, patch));
        System.out.println(res.message());
    }
    private ProductDto readProductDto() {
        System.out.print("Тип (newspaper/magazine/book): ");
        String type = in.nextLine().trim().toLowerCase();
        System.out.print("Название: ");
        String title = in.nextLine().trim();

        return switch (type) {
            case "newspaper" -> {
                System.out.print("Номер: ");
                String issue = in.nextLine().trim();
                System.out.print("Дата выпуска (yyyy-mm-dd): ");
                LocalDate date = LocalDate.parse(in.nextLine().trim());
                yield ProductDto.newspaper(title, issue, date);
            }
            case "magazine" -> {
                System.out.print("Номер: ");
                String issue = in.nextLine().trim();
                System.out.print("Дата выпуска (yyyy-mm-dd): ");
                LocalDate date = LocalDate.parse(in.nextLine().trim());
                System.out.print("Страниц: ");
                int pages = Integer.parseInt(in.nextLine().trim());
                yield ProductDto.magazine(title, issue, date, pages);
            }
            case "book" -> {
                System.out.print("Автор: ");
                String author = in.nextLine().trim();
                System.out.print("Издательство: ");
                String publisher = in.nextLine().trim();
                System.out.print("Страниц: ");
                int pages = Integer.parseInt(in.nextLine().trim());
                yield ProductDto.book(title, author, publisher, pages);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        };
    }
    private ProductDto readPartialProduct() {
        System.out.print("Новое название: ");
        String title = emptyToNull(in.nextLine());
        System.out.print("Новый номер (газета/журнал): ");
        String issue = emptyToNull(in.nextLine());
        System.out.print("Новая дата выпуска (yyyy-mm-dd): ");
        String date = emptyToNull(in.nextLine());
        System.out.print("Новые страницы (число): ");
        String pages = emptyToNull(in.nextLine());
        System.out.print("Новый автор (для книги): ");
        String author = emptyToNull(in.nextLine());
        System.out.print("Новое издательство (для книги): ");
        String publisher = emptyToNull(in.nextLine());
        return ProductDto.patch(title, issue, date, pages, author, publisher);
    }

    private static String emptyToNull(String s) {
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
