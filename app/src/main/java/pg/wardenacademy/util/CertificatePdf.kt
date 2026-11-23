package pg.wardenacademy.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Draws an A4-landscape certificate with the platform PdfDocument API (no extra libraries). */
object CertificatePdf {

    data class Details(
        val name: String,
        val staffId: String,
        val province: String,
        val rank: String,
        val points: Int,
        val modules: Int,
        val averagePercent: Int,
        val issuedAt: Long = System.currentTimeMillis()
    )

    fun create(context: Context, d: Details): File {
        val width = 842   // A4 landscape, in points
        val height = 595
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
        val c = page.canvas

        val red = Color.rgb(183, 28, 28)
        val gold = Color.rgb(249, 168, 37)
        val black = Color.rgb(26, 26, 26)

        // Borders
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        border.color = red; border.strokeWidth = 14f
        c.drawRect(RectF(14f, 14f, width - 14f, height - 14f), border)
        border.color = gold; border.strokeWidth = 4f
        c.drawRect(RectF(32f, 32f, width - 32f, height - 32f), border)

        fun text(s: String, y: Float, size: Float, color: Int = black, bold: Boolean = false, italic: Boolean = false) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(
                    Typeface.SERIF,
                    when {
                        bold && italic -> Typeface.BOLD_ITALIC
                        bold -> Typeface.BOLD
                        italic -> Typeface.ITALIC
                        else -> Typeface.NORMAL
                    }
                )
            }
            c.drawText(s, width / 2f, y, p)
        }

        text("WARDEN ACADEMY PNG", 92f, 18f, red, bold = true)
        text("Certificate of Completion", 140f, 40f, black, bold = true)
        text("This is to certify that", 190f, 16f, italic = true)
        text(d.name, 240f, 36f, red, bold = true)
        text("Staff ID ${d.staffId}  •  ${d.province}", 268f, 13f)
        text("has completed all ${d.modules} modules of the training programme", 310f, 16f)
        text("Functions of the Mining Warden under the Mining Act 1992 (Papua New Guinea)", 336f, 16f, bold = true)
        text("Rank achieved: ${d.rank}   •   Points: ${d.points}   •   Average best score: ${d.averagePercent}%", 378f, 14f)

        val date = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(d.issuedAt))
        text("Issued $date", 412f, 13f, italic = true)

        // Signature lines
        val line = Paint().apply { color = black; strokeWidth = 1f }
        c.drawLine(140f, 490f, 340f, 490f, line)
        c.drawLine(width - 340f, 490f, width - 140f, 490f, line)
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = black; textSize = 11f; textAlign = Paint.Align.CENTER }
        c.drawText("Training Coordinator", 240f, 506f, small)
        c.drawText("Chief Warden", width - 240f, 506f, small)

        val ref = "Certificate ref: WA-${d.staffId}-${d.issuedAt / 1000}"
        c.drawText(ref, width / 2f, 548f, small.apply { color = Color.GRAY; textSize = 9f })

        doc.finishPage(page)
        val dir = File(context.cacheDir, "certificates").apply { mkdirs() }
        val safeName = d.name.replace(Regex("[^A-Za-z0-9]+"), "_")
        val file = File(dir, "Warden_Certificate_$safeName.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share certificate"))
    }
}
