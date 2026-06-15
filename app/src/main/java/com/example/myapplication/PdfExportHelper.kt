package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.myapplication.data.Assessment
import com.example.myapplication.data.Student
import com.example.myapplication.data.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExportHelper {

    suspend fun exportGradesToPdf(
        context: Context,
        subject: Subject,
        students: List<Student>,
        assessments: Map<String, Assessment?>,
        calculateGPA: (Assessment?) -> Double
    ) {
        withContext(Dispatchers.IO) {
            try {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                var page = document.startPage(pageInfo)
                var canvas = page.canvas

                val titlePaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 24f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                val headerPaint = Paint().apply {
                    color = Color.DKGRAY
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                val textPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 12f
                }

                val linePaint = Paint().apply {
                    color = Color.LTGRAY
                    strokeWidth = 1f
                }

                var yOffset = 50f
                val margin = 50f

                // Title
                canvas.drawText("Grade Report: ${subject.courseName}", margin, yOffset, titlePaint)
                yOffset += 40f
                canvas.drawText("Course Code: ${subject.subjectId}", margin, yOffset, textPaint)
                yOffset += 20f
                canvas.drawText("Total Students: ${students.size}", margin, yOffset, textPaint)
                yOffset += 40f

                // Table Header
                canvas.drawLine(margin, yOffset - 15, pageInfo.pageWidth - margin, yOffset - 15, linePaint)
                canvas.drawText("Student Name", margin, yOffset, headerPaint)
                canvas.drawText("Email", margin + 150f, yOffset, headerPaint)
                canvas.drawText("GPA", margin + 350f, yOffset, headerPaint)
                canvas.drawText("Status", margin + 420f, yOffset, headerPaint)
                yOffset += 10f
                canvas.drawLine(margin, yOffset, pageInfo.pageWidth - margin, yOffset, linePaint)
                yOffset += 25f

                // Rows
                for (student in students) {
                    // Check page break
                    if (yOffset > pageInfo.pageHeight - 50) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yOffset = 50f
                    }

                    val assessment = assessments[student.studentId]
                    val gpa = calculateGPA(assessment)
                    val status = if (gpa >= 2.0) "Pass" else "Fail"

                    canvas.drawText(student.studentName.take(20), margin, yOffset, textPaint)
                    canvas.drawText(student.email.take(25), margin + 150f, yOffset, textPaint)
                    canvas.drawText(String.format("%.2f", gpa), margin + 350f, yOffset, textPaint)
                    
                    textPaint.color = if (gpa >= 2.0) Color.rgb(0, 150, 0) else Color.RED
                    canvas.drawText(status, margin + 420f, yOffset, textPaint)
                    textPaint.color = Color.BLACK // Reset

                    yOffset += 20f
                    canvas.drawLine(margin, yOffset - 10, pageInfo.pageWidth - margin, yOffset - 10, linePaint)
                    yOffset += 10f
                }

                document.finishPage(page)

                // Save file
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "GradeReport_${subject.courseName.replace(" ", "_")}.pdf"
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
                document.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to Downloads: $fileName", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
