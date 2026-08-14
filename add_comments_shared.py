import os
import re

ROOT = "shared-lib/src/main/java"


def class_name_from_path(path):
    return os.path.splitext(os.path.basename(path))[0]


def file_desc(path):
    rel = os.path.relpath(path, ROOT).replace("\\", "/")
    parts = rel.split("/")
    pkg_parts = parts[3:-1]
    name = class_name_from_path(path)
    pkg = ".".join(pkg_parts)
    return class_desc(name, pkg)


def add_file_header(content, path):
    lines = content.split("\n")
    first = 0
    while first < len(lines) and lines[first].strip() == "":
        first += 1
    if first >= len(lines):
        return content
    if lines[first].strip().startswith("/*"):
        return content
    desc = file_desc(path)
    header = f"""/**
 * {desc}。
 */"""
    return header + "\n" + content


def has_javadoc_above(lines, idx):
    """检查上方（跳过空行与注解行）是否存在 Javadoc 注释。"""
    i = idx - 1
    while i >= 0:
        stripped = lines[i].strip()
        if stripped == "" or stripped.startswith("@"):
            i -= 1
            continue
        break
    if i < 0:
        return False
    if lines[i].strip() != "*/":
        return False
    for j in range(i, max(-1, i - 50), -1):
        if lines[j].strip().startswith("/**"):
            return True
    return False


def class_desc(name, pkg):
    """根据类名与包名生成中文描述。"""
    suffix_map = {
        "Request": "数据传输请求对象",
        "Response": "数据传输响应对象",
        "DTO": "数据传输对象",
        "Mapper": "MyBatis 数据访问接口",
        "Service": "业务服务类",
        "Client": "客户端实现类",
        "ClientRegistry": "客户端策略注册表",
        "Config": "配置类",
        "Properties": "配置属性类",
        "Interceptor": "拦截器",
        "Handler": "处理器",
        "Util": "工具类",
        "Converter": "转换器",
        "Exception": "业务异常",
        "Result": "统一响应结果",
        "Enum": "枚举",
    }
    for suffix, desc in suffix_map.items():
        if name.endswith(suffix):
            prefix = name[: -len(suffix)]
            prefix = prefix.replace("Kb", "知识库").replace("Admin", "后台管理").replace("Sys", "系统")
            return f"{prefix}{desc}"
    if ".entity." in pkg or pkg.endswith(".entity"):
        return f"{name} 实体"
    if ".enums." in pkg or pkg.endswith(".enums"):
        return f"{name} 枚举"
    if ".dto." in pkg or pkg.endswith(".dto"):
        return f"{name} 数据传输对象"
    if ".mapper." in pkg or pkg.endswith(".mapper"):
        return f"{name} 数据访问接口"
    if ".config." in pkg or pkg.endswith(".config"):
        return f"{name} 配置"
    if ".util." in pkg or pkg.endswith(".util"):
        return f"{name} 工具类"
    if ".common." in pkg or pkg.endswith(".common"):
        return f"{name} 公共组件"
    return f"{name}"


class_re = re.compile(
    r"^\s*(?:(?:public|protected|private)\s+)?(?:(?:abstract|final|static)\s+)?(class|interface|enum)\s+(\w+)"
)
method_re = re.compile(
    r"^(\s*)(?:(public|protected)\s+)(?:(?:static|final|abstract|synchronized|default|native)\s+)*([\w<>?,\[\]\s]+)\s+(\w+)\s*\("
)
interface_method_re = re.compile(
    r"^(\s*)([\w<>?,\[\]\s]+)\s+(\w+)\s*\(\s*[^)]*\)\s*;"
)
ctor_re = re.compile(
    r"^(\s*)(?:(public|protected)\s+)(\w+)\s*\("
)
field_re = re.compile(
    r"^(\s*)(?:(?:public|protected|private|static|final)\s+)+([\w<>?,\[\]\s]+)\s+(\w+)\s*;"
)


def count_braces(line):
    """统计该行中花括号的增减（忽略字符串字面量）。"""
    delta = 0
    in_string = False
    escape = False
    for ch in line:
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
        else:
            if ch == '"':
                in_string = True
            elif ch == '{':
                delta += 1
            elif ch == '}':
                delta -= 1
    return delta


def extract_params(signature_line):
    """从方法签名行中提取参数名（简单实现，不处理多行签名）。"""
    params = []
    if "(" in signature_line and ")" in signature_line:
        inner = signature_line[signature_line.find("(") + 1:signature_line.rfind(")")]
        parts = []
        depth = 0
        current = []
        for ch in inner:
            if ch == '<':
                depth += 1
            elif ch == '>':
                depth -= 1
            elif ch == ',' and depth == 0:
                parts.append(''.join(current))
                current = []
                continue
            current.append(ch)
        if current:
            parts.append(''.join(current))
        for p in parts:
            p = p.strip()
            if not p:
                continue
            tokens = p.split()
            if len(tokens) >= 2:
                params.append(tokens[-1])
    return params


def build_method_javadoc(indent, method_name, return_type, signature_line):
    params = extract_params(signature_line)
    lines = [indent + "/**"]
    # 根据方法名生成更贴切的描述
    desc = None
    if method_name.startswith("get") and len(method_name) > 3 and return_type != "void":
        prop = method_name[3:]
        desc = f"获取 {prop}。"
    elif method_name.startswith("set") and len(method_name) > 3:
        prop = method_name[3:]
        desc = f"设置 {prop}。"
    elif method_name.startswith("is") and len(method_name) > 2 and return_type in ("boolean", "Boolean"):
        prop = method_name[2:]
        desc = f"判断是否为 {prop}。"
    elif method_name == "equals" and len(params) == 1:
        desc = "判断两个对象是否相等。"
    elif method_name == "hashCode" and not params:
        desc = "计算对象哈希码。"
    elif method_name == "toString" and not params:
        desc = "返回对象字符串表示。"
    if not desc:
        desc = f"{method_name} 方法。"
    lines.append(indent + f" * {desc}")
    for p in params:
        lines.append(indent + f" * @param {p} 参数说明")
    if return_type and return_type != "void":
        lines.append(indent + " * @return 返回值说明")
    lines.append(indent + " */")
    return lines


def build_constructor_javadoc(indent, ctor_name, signature_line):
    params = extract_params(signature_line)
    lines = [indent + "/**"]
    lines.append(indent + f" * 构造 {ctor_name} 实例。")
    for p in params:
        lines.append(indent + f" * @param {p} 参数说明")
    lines.append(indent + " */")
    return lines


def insert_javadoc_before_annotations(new_lines, javadoc_lines):
    """将 Javadoc 插入到已缓存的注解行之前，保持空白行结构。"""
    annotations = []
    while new_lines and new_lines[-1].strip().startswith("@"):
        annotations.insert(0, new_lines.pop())
    blanks = []
    while new_lines and new_lines[-1].strip() == "":
        blanks.insert(0, new_lines.pop())
    new_lines.extend(blanks)
    new_lines.extend(javadoc_lines)
    new_lines.extend(annotations)


def package_from_content(content):
    m = re.search(r"^\s*package\s+([\w.]+);", content, re.MULTILINE)
    return m.group(1) if m else ""


def add_javadocs(content, path):
    pkg = package_from_content(content)
    lines = content.split("\n")
    new_lines = []
    brace_level = 0
    in_multiline = False
    in_interface = False
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # 处理多行注释
        if in_multiline:
            new_lines.append(line)
            if "*/" in line:
                in_multiline = False
            i += 1
            continue
        if stripped.startswith("/*"):
            in_multiline = True
            new_lines.append(line)
            if "*/" in line:
                in_multiline = False
            i += 1
            continue

        # 顶层类/接口/枚举声明
        m = class_re.match(line)
        if m and brace_level == 0:
            type_name = m.group(1)
            name = m.group(2)
            in_interface = (type_name == "interface")
            if not has_javadoc_above(lines, i):
                desc = class_desc(name, pkg)
                type_label = "枚举" if type_name == "enum" else "接口" if type_name == "interface" else "类"
                javadoc_lines = ["/**", f" * {desc}。", f" * 本{type_label}定义了 {name} 的公共契约与数据结构。", " */"]
                insert_javadoc_before_annotations(new_lines, javadoc_lines)
            new_lines.append(line)
            brace_level += count_braces(line)
            i += 1
            continue

        # 类内部的 public/protected 方法或构造方法（仅处理顶层类作用域，brace_level == 1）
        if brace_level == 1:
            if not stripped.startswith("@"):
                m = method_re.match(line)
                if m:
                    indent = m.group(1)
                    return_type = m.group(3).strip()
                    method_name = m.group(4)
                    if not has_javadoc_above(lines, i):
                        insert_javadoc_before_annotations(
                            new_lines,
                            build_method_javadoc(indent, method_name, return_type, line)
                        )
                    new_lines.append(line)
                    brace_level += count_braces(line)
                    i += 1
                    continue
                m = ctor_re.match(line)
                if m:
                    indent = m.group(1)
                    ctor_name = m.group(3)
                    if not has_javadoc_above(lines, i):
                        insert_javadoc_before_annotations(
                            new_lines,
                            build_constructor_javadoc(indent, ctor_name, line)
                        )
                    new_lines.append(line)
                    brace_level += count_braces(line)
                    i += 1
                    continue
                # 接口中的方法（无显式 public 修饰符）
                if in_interface:
                    m = interface_method_re.match(line)
                    if m:
                        indent = m.group(1)
                        return_type = m.group(2).strip()
                        method_name = m.group(3)
                        if not has_javadoc_above(lines, i):
                            insert_javadoc_before_annotations(
                                new_lines,
                                build_method_javadoc(indent, method_name, return_type, line)
                            )
                        new_lines.append(line)
                        brace_level += count_braces(line)
                        i += 1
                        continue

        new_lines.append(line)
        brace_level += count_braces(line)
        i += 1

    return "\n".join(new_lines)


def add_field_comments(content):
    lines = content.split("\n")
    new_lines = []
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("/*"):
            new_lines.append(line)
            continue
        m = field_re.match(line)
        if m and "//" not in line:
            fname = m.group(3)
            line = line + f" // {fname} 字段"
        new_lines.append(line)
    return "\n".join(new_lines)


def main():
    count = 0
    for dirpath, _, filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith(".java"):
                path = os.path.join(dirpath, fn)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                new_content = add_file_header(content, path)
                new_content = add_javadocs(new_content, path)
                new_content = add_field_comments(new_content)
                if new_content != content:
                    with open(path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    count += 1
    print(f"Modified {count} files")


if __name__ == "__main__":
    main()
