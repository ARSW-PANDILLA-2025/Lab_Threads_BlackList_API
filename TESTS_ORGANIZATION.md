# Tests Organization - Lab Threads BlackList API

## 📋 Overview
Los tests han sido reorganizados y renombrados para eliminar duplicados y seguir una nomenclatura clara basada en las actividades del laboratorio.

## 🧪 Test Structure

### **Test 1** - CountThread Tests (`Test1CountThreadTest.java`)
**Location:** `src/test/java/co/eci/blacklist/labs/part1/`
**Activity:** Actividad 1 - Clase CountThread

**Tests incluidos:**
- `test1_1_shouldCountInSimpleRange()` - Verifica conteo en rango simple
- `test1_2_shouldHandleSingleNumberRange()` - Maneja rangos de un solo número
- `test1_3_shouldHandleZeroBasedRange()` - Verifica rangos que incluyen cero
- `test1_4_shouldRunMultipleThreadsConcurrently()` - Múltiples hilos concurrentes
- `test1_5_shouldShowDifferenceBetweenStartAndRun()` - **Demuestra la diferencia entre start() y run()**

### **Test 2** - BlacklistChecker2 Tests (`Test2BlacklistChecker2Test.java`)
**Location:** `src/test/java/co/eci/blacklist/labs/part2/`
**Activity:** Actividad 2 - Implementación con Thread clásico

**Tests incluidos:**
- `test2_1_shouldFindIPInAssignedRange()` - Encuentra IP en rango asignado
- `test2_2_shouldWorkWithMultipleThreadsOnDifferentSegments()` - Múltiples hilos en segmentos
- `test2_3_shouldImplementCompleteCheckHostLogic()` - Lógica completa de checkHost
- `test2_4_shouldHandleRemainderInServerDivision()` - Manejo de resto en división
- `test2_5_shouldMaintainThreadSafetyInResults()` - Thread safety en resultados

### **Test 3** - Specific IPs Tests (`Test3SpecificIPsTest.java`)
**Location:** `src/test/java/co/eci/blacklist/labs/part2/`
**Activity:** Actividad 3 - Pruebas con IPs específicas

**Tests incluidos:**
- `test3_1_shouldDetectConcentratedIPQuickly()` - **200.24.34.55** (matches concentrados)
- `test3_2_shouldHandleDispersedIPCorrectly()` - **202.24.34.55** (matches dispersos)
- `test3_3_shouldHandleCleanIPWorstCase()` - **212.24.24.55** (sin matches - peor caso)
- `test3_4_shouldImprovePerformanceWithMoreThreads()` - Rendimiento con más hilos
- `test3_5_shouldDemonstrateEarlyStoppingBehavior()` - Comportamiento de early stopping
- `test3_6_shouldScaleWithHighThreadCount()` - Escalabilidad con muchos hilos
- `test3_7_shouldMatchActivityExpectedBehavior()` - Comportamiento esperado por actividad
- `test3_8_comprehensivePerformanceAnalysisWithLogging()` - **Análisis completo con logging detallado**

**📊 Funcionalidad especial:**
- El **Test 3.8** incluye toda la funcionalidad de logging del original `TestSpecificIPs.java`
- Imprime análisis detallado de performance para diferentes cantidades de hilos (1, 2, 4, 8, 16)
- Muestra tabla comparativa de resultados
- Incluye observaciones y análisis de comportamiento

### **Test 4** - Integration Tests (`Test4BlacklistCheckerIntegrationTest.java`)
**Location:** `src/test/java/co/eci/blacklist/domain/`
**Activity:** Tests de integración con implementación principal

**Tests incluidos:**
- `test4_1_earlyStopShouldAvoidScanningAllServers()` - Early stopping con virtual threads

### **Test 5** - API Tests (`Test5BlacklistControllerApiTest.java`)
**Location:** `src/test/java/co/eci/blacklist/api/`
**Activity:** Tests de API REST

**Tests incluidos:**
- `test5_1_shouldReturn200ForValidIPv4()` - API con IP válida
- `test5_2_shouldReturn400ForInvalidIPv4()` - API con IP inválida

## 🗑️ Tests Eliminados (Duplicados)
Los siguientes archivos fueron eliminados por ser duplicados:

- ❌ `TestThreadedImplementation.java` (en domain)
- ❌ `TestSpecificIPs.java` (en domain, main/part2 - **funcionalidad integrada en Test3.8**)
- ❌ `SimpleIPTest.java` (en domain)
- ❌ `BlacklistCheckerTest2.java` (en domain)
- ❌ `BlacklistCheckerTest.java` (renombrado a Test4)

## 🎯 Key Differences Demonstrated

### **Actividad 1: start() vs run()**
El **Test 1.5** demuestra específicamente la diferencia entre usar `start()` y `run()`:
- `start()`: Crea un nuevo hilo y ejecuta en paralelo
- `run()`: Ejecuta en el hilo actual (no hay paralelización)

### **Actividad 2: Thread Implementation**
Los **Tests 2.x** verifican la implementación completa incluyendo:
- División correcta de servidores entre N hilos
- Manejo de resto cuando N no divide exactamente
- Uso correcto de `start()` y `join()`
- Thread safety en agregación de resultados

### **Actividad 3: Performance Analysis**
Los **Tests 3.x** validan el comportamiento con las IPs específicas:
- **200.24.34.55**: Early stopping efectivo (matches concentrados)
- **202.24.34.55**: Early stopping moderado (matches dispersos)  
- **212.24.24.55**: Sin early stopping (peor caso - todos los servidores)

### **🌟 Funcionalidad Especial - Test 3.8**
El **Test 3.8** combina testing unitario con logging detallado:
- **Análisis exhaustivo**: Prueba las 3 IPs con 1, 2, 4, 8 y 16 hilos
- **Logging completo**: Muestra resultados detallados como el original `TestSpecificIPs.java`
- **Tabla comparativa**: Resumen visual de performance
- **Validación automática**: Assert para garantizar que el test pasa
- **Observaciones**: Análisis automático del comportamiento observado

## 🚀 Running Tests

Para ejecutar todos los tests:
```bash
mvn test
```

Para ejecutar tests específicos por actividad:
```bash
# Actividad 1
mvn test -Dtest=Test1CountThreadTest

# Actividad 2  
mvn test -Dtest=Test2BlacklistChecker2Test

# Actividad 3
mvn test -Dtest=Test3SpecificIPsTest

# Integration Tests
mvn test -Dtest=Test4BlacklistCheckerIntegrationTest

# API Tests
mvn test -Dtest=Test5BlacklistControllerApiTest
```

## 📊 Expected Results

### Performance Hierarchy (servers checked):
```
Clean IP (212.24.24.55) >= Dispersed IP (202.24.34.55) >= Concentrated IP (200.24.34.55)
```

### Thread Safety Validation:
- Cada hilo maneja su segmento asignado
- Sin duplicados entre hilos
- Agregación correcta de resultados
- Early stopping coordinado

### API Validation:
- IPv4 válidos retornan 200 OK
- IPv4 inválidos retornan 400 Bad Request
- Parámetros de threads son respetados
