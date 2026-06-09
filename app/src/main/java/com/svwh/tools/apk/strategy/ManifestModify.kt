package com.svwh.tools.apk.strategy

import android.content.Context
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.axml.AXMLEncoder
import com.svwh.tools.constant.ApkConstant
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:59
 */
class ManifestModify : IApkModification {

    override fun modify(apkProcessorContext: ApkProcessorContext): IApkModification {
        var axmlDocument = apkProcessorContext.axmlDocument
        val manifestNode = axmlDocument!!.getElementsByTagName("manifest").item(0);
        var childNodes = manifestNode.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            // 寻找Application入口类，修改并保存入口类
            if (node.nodeName.equals("application")) {
                // 1. 配置唯一标识
                val killerMetadata = axmlDocument.createElement("meta-data");
                killerMetadata.setAttribute("android:name", ApkConstant.NO_ENV_METADATA_NAME)
                killerMetadata.setAttribute("android:value", ApkConstant.NO_ENV_METADATA_VALUE)
                node.appendChild(killerMetadata);
                // 2. 构建provider
                val providerElement = axmlDocument.createElement("provider");
                providerElement.setAttribute(
                    "android:name",
                    "com.svwh.noenvhook.log.HookLogProvider"
                )
                providerElement.setAttribute("android:exported", "true");
                providerElement.setAttribute(
                    "android:authorities",
                    "${apkProcessorContext.packageName}.killer_hook_provider"
                )

                node.appendChild(providerElement);
                node.attributes.getNamedItem("android:extractNativeLibs")
                    ?.takeIf { it.nodeValue == "false" }
                    ?.nodeValue = "true"
                // 3. 配置入口类
                val namedItem = node.attributes.getNamedItem("android:name")
                if (namedItem == null) {
                    val n = axmlDocument.createAttribute("android:name")
                    n.value = ApkConstant.BASE_APPLICATION_NAME
                    node.attributes.setNamedItem(n)
                }

                break;
            }
        }
        apkProcessorContext.extraDataNodes.add(
            ExtraDataNode(
                "AndroidManifest.xml",
                xmlIs(axmlDocument, apkProcessorContext.context)
            )
        )
        return this
    }


    /**
     * 返回Android.xml的输入流操作
     */
    private fun xmlIs(document: Document, context: Context): ByteArrayInputStream {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.outputProperties[OutputKeys.INDENT] = "yes"
        transformer.outputProperties["{http://xml.apache.org/xslt}indent-amount"] = "4";
        val outputStream = ByteArrayOutputStream()
        transformer.transform(DOMSource(document), StreamResult(outputStream))
        val encoder = AXMLEncoder()
        return ByteArrayInputStream(
            encoder.encodeString(
                context,
                String(outputStream.toByteArray())
            )
        )
    }
}
