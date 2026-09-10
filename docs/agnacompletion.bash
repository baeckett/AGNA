# agna CLI completion for bash
# source this file from ~/.bashrc, e.g.:
#   [ -f /usr/local/share/agna/agnacompletion.bash ] &&
#       . /usr/local/share/agna/agnacompletion.bash
#
# Completes commands, metric names, analyses, transform ops, layouts,
# formats, types and option values, with file completion as fallback.

_agna()
{
    local cur prev word
    cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"

    local commands="version info analyse convert transform draw generate matrix nodes ego components metrics distance diff --help --version"
    local analyses="basic density cohesion nodal indegree outdegree emission reception determination status geodesics open-chain eccentricity diameter bavelas closeness fareness betweenness prestige cliques full"
    local metric_names="density diameter eccentricity closeness betweenness indegree outdegree total-degree emission reception status determination geodesics"
    local transform_ops="transpose symmetrize-max symmetrize-sum symmetrize-below normalize-binary normalize-threshold: add-scalar: multiply-scalar: square merge: delete-node: delete-nodes: isolate: merge-nodes: remove-outsiders"
    local layouts="circular random spring grid concentric"
    local formats="agn txt csv net graphml gml graphson xls"
    local types="random star circular"

    # value completion for the previous token
    case "$prev" in
        --layout) COMPREPLY=( $(compgen -W "$layouts" -- "$cur") ); return ;;
        --type)   COMPREPLY=( $(compgen -W "$types" -- "$cur") ); return ;;
        --format) COMPREPLY=( $(compgen -W "csv tsv json" -- "$cur") ); return ;;
        --in-format|--out-format) COMPREPLY=( $(compgen -W "$formats" -- "$cur") ); return ;;
        --op)     COMPREPLY=( $(compgen -W "$transform_ops" -- "$cur") ); return ;;
    esac

    if [[ $COMP_CWORD -eq 1 ]]; then
        COMPREPLY=( $(compgen -W "$commands" -- "$cur") )
        return
    fi

    local cmd="${COMP_WORDS[1]}"
    case "$cmd" in
        analyse)
            COMPREPLY=( $(compgen -W "$analyses --all --out" -- "$cur") )
            ;;
        metrics)
            COMPREPLY=( $(compgen -W "$metric_names --all --format --out" -- "$cur") )
            ;;
        transform)
            COMPREPLY=( $(compgen -W "$transform_ops --in-format --out-format" -- "$cur") )
            ;;
        draw)
            COMPREPLY=( $(compgen -W "--layout --size --labels --out" -- "$cur") )
            ;;
        generate)
            COMPREPLY=( $(compgen -W "--nodes --out --type --seed --degree" -- "$cur") )
            ;;
        convert)
            COMPREPLY=( $(compgen -W "--in-format --out-format" -- "$cur") )
            ;;
        ego|distance)
            COMPREPLY=( $(compgen -W "--node --from --to --out" -- "$cur") )
            ;;
        *)  # FILE arguments everywhere else
            COMPREPLY=( $(compgen -f -- "$cur") )
            ;;
    esac
    return
}

complete -o default -F _agna agna
complete -o default -F _agna java