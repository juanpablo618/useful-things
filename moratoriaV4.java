
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class CalculadoraMoratoriaV4 {


    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter RANGO_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final LocalDate LIMITE_24476 = LocalDate.of(1993, Month.SEPTEMBER, 30);
    private static final LocalDate INICIO_27705 = LocalDate.of(1993, Month.OCTOBER, 1);
    private static final LocalDate FIN_27705 = LocalDate.of(2012, Month.MARCH, 31);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("📅 Ingrese fecha de nacimiento del titular (formato: dd/MM/yyyy):");
        String fechaNacimientoStr = scanner.nextLine();
        LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr, RANGO_FORMAT);

        System.out.println("\n📋 Ingrese los rangos de períodos con aportes registrados (formato: dd/MM/yyyy - dd/MM/yyyy, separados por coma):");
        System.out.println("Ejemplo: 01/03/1987 - 31/12/1989, 01/10/1993 - 30/06/1995");
        String rangosInput = scanner.nextLine();

        Set<String> periodosConAportes = Arrays.stream(rangosInput.split(","))
                .map(String::trim)
                .flatMap(rango -> expandirRango(rango).stream())
                .collect(Collectors.toSet());

        int totalAportados = periodosConAportes.size();

        LocalDate fechaInicioMoratoria = fechaNacimiento.plusYears(18);
        List<String> todosLosPeriodos = generarPeriodos(fechaInicioMoratoria, FIN_27705);

        List<String> periodosSinAportes = todosLosPeriodos.stream()
                .filter(p -> !periodosConAportes.contains(p))
                .collect(Collectors.toList());

        List<String> moratoria24476 = periodosSinAportes.stream()
                .filter(p -> !parsePeriodo(p).isAfter(LIMITE_24476))
                .collect(Collectors.toList());

        List<String> moratoria27705 = periodosSinAportes.stream()
                .filter(p -> {
                    LocalDate fecha = parsePeriodo(p);
                    return (!fecha.isBefore(INICIO_27705) && !fecha.isAfter(FIN_27705));
                }).collect(Collectors.toList());

        List<String[]> seleccionFinal = limitarA360Meses(totalAportados, moratoria24476, moratoria27705);

        // Salidas
        System.out.println("\n════════════════════════════════════════════");
        System.out.println("🧾 PERÍODOS REGULARIZABLES SEGÚN CADA MORATORIA");
        System.out.println("════════════════════════════════════════════");

        System.out.println("\n📘 Ley 24.476 (Desde los 18 años hasta 30/09/1993):");
        imprimirPeriodos(moratoria24476);

        System.out.println("\n📙 Ley 27.705 (Desde 01/10/1993 hasta 31/03/2012):");
        imprimirPeriodos(moratoria27705);

        System.out.println("\n════════════════════════════════════════════");
        System.out.println("✅ PERÍODOS SELECCIONADOS PARA REGULARIZAR (MÁXIMO 30 AÑOS = 360 MESES)");
        System.out.println("════════════════════════════════════════════");
        seleccionFinal.forEach(p -> System.out.printf("%s  ← Ley %s\n", p[0], p[1]));

        int totalSeleccionados = totalAportados + seleccionFinal.size();

        System.out.println("\n════════════════════════════════════════════");
        System.out.println("📊 RESUMEN GENERAL");
        System.out.println("════════════════════════════════════════════");
        System.out.printf("Meses con aportes registrados: %d\n", totalAportados);
        System.out.printf("Meses agregados por Ley 24.476: %d\n", seleccionFinal.stream().filter(p -> p[1].equals("24.476")).count());
        System.out.printf("Meses agregados por Ley 27.705: %d\n", seleccionFinal.stream().filter(p -> p[1].equals("27.705")).count());
        System.out.printf("Total meses computados para jubilación: %d\n", totalSeleccionados);

        if (totalSeleccionados == 360) {
            System.out.println("✅ Se alcanzó el máximo de 360 meses permitidos.");
        } else {
            System.out.printf("⚠️ Faltan %d meses para completar 30 años.\n", 360 - totalSeleccionados);
        }

        scanner.close();
    }

    private static List<String> generarPeriodos(LocalDate inicio, LocalDate fin) {
        List<String> periodos = new ArrayList<>();
        while (!inicio.isAfter(fin)) {
            periodos.add(inicio.format(OUTPUT_FORMAT));
            inicio = inicio.plusMonths(1);
        }
        return periodos;
    }

    private static LocalDate parsePeriodo(String periodo) {
        return LocalDate.parse("01/" + periodo, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static List<String[]> limitarA360Meses(int yaAportados, List<String> moratoria24476, List<String> moratoria27705) {
        int max = 360;
        int restantes = max - yaAportados;
        List<String[]> resultado = new ArrayList<>();

        for (String p : moratoria24476) {
            if (resultado.size() < restantes) {
                resultado.add(new String[]{p, "24.476"});
            } else break;
        }

        for (String p : moratoria27705) {
            if (resultado.size() < restantes) {
                resultado.add(new String[]{p, "27.705"});
            } else break;
        }

        return resultado;
    }

    private static void imprimirPeriodos(List<String> periodos) {
        for (int i = 0; i < periodos.size(); i++) {
            System.out.printf("%s%s", periodos.get(i), (i + 1) % 10 == 0 ? "\n" : "  ");
        }
        if (periodos.size() % 10 != 0) {
            System.out.println();
        }
    }

    private static List<String> expandirRango(String rango) {
        try {
            String[] partes = rango.split("-");
            if (partes.length != 2) {
                System.out.println("❌ Rango inválido: " + rango);
                return Collections.emptyList();
            }

            LocalDate desde = LocalDate.parse(partes[0].trim(), RANGO_FORMAT).withDayOfMonth(1);
            LocalDate hasta = LocalDate.parse(partes[1].trim(), RANGO_FORMAT).withDayOfMonth(1);

            List<String> periodos = new ArrayList<>();
            while (!desde.isAfter(hasta)) {
                periodos.add(desde.format(OUTPUT_FORMAT));
                desde = desde.plusMonths(1);
            }

            return periodos;
        } catch (Exception e) {
            System.out.println("❌ Error al procesar rango: " + rango);
            return Collections.emptyList();
        }
    }


}
